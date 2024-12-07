package Network

import Network._

// Import necessary scala libraries
import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.{Map, ListBuffer}
import scala.util.{Success, Failure}
import scala.annotation.tailrec

// Import necessary java libraries
import java.net.InetAddress
import java.util.concurrent.TimeUnit

// Import logging libraries
import org.apache.logging.log4j.scala.Logging

// Import necessary project libraries
import Common._
import Core._
import Core.Table._
import Core.Key._
import Core.Block._

// Import gRPC libraries
import io.grpc.{Server, ManagedChannel, ManagedChannelBuilder, ServerBuilder, Status}
import io.grpc.stub.StreamObserver
import com.google.protobuf.ByteString

// Import protobuf messages and services
import message.establishment.{EstablishRequest, EstablishResponse}
import message.sampling.{SampleRequest, SampleResponse}
import message.partitioning.{PartitionRequest, PartitionResponse}
import message.shuffling.{ShuffleRunRequest, ShuffleRunResponse, ShuffleExchangeRequest, ShuffleExchangeResponse}
import message.merging.{MergeRequest, MergeResponse}
import message.verification.{VerificationRequest, VerificationResponse}
import message.service.{MasterServiceGrpc, WorkerServiceGrpc}
import message.common.{DataChunk, KeyRangeTableRow, KeyRangeTable}

// Define the Network object
object Network {
  type IPAddr = String
  type Port = Int
  type Node = (IPAddr, Port)
}

class NetworkServer(port: Int, numberOfWorkers: Int, executionContext: ExecutionContext)
    extends Logging {

  var server: Server = null
  var state: MasterState = MasterInitial
  var clients: ListBuffer[Node] = ListBuffer.empty
  var sample: List[Key] = List.empty[Key]

  var channels: Seq[ManagedChannel] = null
  var stubs: Seq[WorkerServiceGrpc.WorkerServiceStub] = null

  def start(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(MasterServiceGrpc.bindService(new ServerImpl(clients), executionContext))
      .build()
      .start()
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdown.awaitTermination(1, TimeUnit.SECONDS)
      state = MasterFinished
    }
  }

  def createChannels(): Unit = {
    channels = clients.map { client =>
      ManagedChannelBuilder
        .forAddress(client._1, client._2)
        .usePlaintext()
        .build()
    }.toSeq
    stubs = channels.map { channel =>
      WorkerServiceGrpc.stub(channel)
    }
  }

  def requestSampling(): Unit = {

    // Perform this to each worker
    val responses: Seq[Future[List[Key]]] = clients.zip(stubs).toSeq.map {
      case (client, stub) => {
        val request =
          SampleRequest(workerIp = client._1, workerPort = client._2, percentageOfSampling = 1)

        val promise = Promise[List[Key]]()
        val buffer = ListBuffer.empty[Key]
        var haveReachedEOF = false

        // Define the response observer to handle the response from the worker
        val responseObserver = new StreamObserver[SampleResponse] {
          override def onNext(value: SampleResponse): Unit = {
            value.sample match {
              case Some(datachunk) =>
                haveReachedEOF = datachunk.isEOF
                // Synchronize buffer
                if (!haveReachedEOF) {
                  buffer.synchronized {
                    buffer += new Key(datachunk.data.toByteArray)
                  }
                }
              case None =>
                onError(new Exception("Received empty data chunk"))
            }
          }

          override def onError(t: Throwable): Unit = {
            promise.failure(t)
          }

          override def onCompleted(): Unit = {
            if (haveReachedEOF) {
              //
              promise.success(buffer.toList)
            } else {
              promise.failure(new Exception("Did not receive EOF"))
            }
          }
        }

        // Request sample data for current worker
        stub.sampleData(request, responseObserver)
        promise.future
      }
    }

    try {
      val allResponses = Await.result(Future.sequence(responses), Duration.Inf)
      sample = allResponses.flatten.toList
      logger.info(s"Number of samples: ${sample.length}")

    } catch {
      case e: Exception => {
        state = MasterReceivedSampleResponseFailure
        logger.error(s"Failed to receive sample data: ${e.getMessage}")
      }
    }
  }

  def createTable(): Table = {
    divideKeyRange().zip(clients).toList
  }

  def requestPartitioning(): Unit = {
    val table = createTable()
    lazy val tableProto: KeyRangeTable = {
      val rows = for {
        (keyRange, node) <- table
        val (ip, port) = node
        val start = ByteString.copyFrom(keyRange.start.key)
        val end = ByteString.copyFrom(keyRange.end.key)
        val range = Some(message.common.KeyRange(start, end))
      } yield KeyRangeTableRow(ip, port, range)
      KeyRangeTable(rows)
    }

    val responses = clients.zip(stubs).toSeq.map {
      case (client, stub) => {
        val promise = Promise[Unit]()

        val request = PartitionRequest(table = Option(tableProto))
        stub.partitionData(request).onComplete {
          case Success(response) => {
            if (response.isPartitioningSuccessful) {
              promise.success(())
            } else {
              promise.failure(new Exception("Partitioning failed"))
            }
          }
          case Failure(e) => promise.failure(e)
        }

        promise.future
      }
    }

    try {
      Await.result(Future.sequence(responses), Duration.Inf)
      state = MasterReceivedPartitionResponse
    } catch {
      case e: Exception => {
        state = MasterReceivedPartitionResponseFailure
        logger.info(s"Failed to receive partition data: ${e.getMessage}")
      }
    }
  }

  def pivot_check(): Unit = {

    val f = Future {}

    f.onComplete {
      case Success(v) => {
        state = MasterReceivedSampleResponse
      }
      case Failure(e) => {
        state = MasterReceivedSampleResponseFailure
      }
    }

  }

  def ipLogging(): Unit = {
    @tailrec
    def clientIPLogging(clients: List[Node]): Unit = {
      assert(clients != Nil)
      logger.info(s"[Master] Worker IP - ${clients.head._1}")
      if (clients.tail != Nil) clientIPLogging(clients.tail)
    }
    val ip = InetAddress.getLocalHost.getAddress
    logger.info(s"[Master] Master IP:Port - ${ip(0).toString}.${ip(1).toString}.${ip(
        2).toString}.${ip(3).toString}:${port.toString}")
    clientIPLogging(clients.toList)
  }

  def divideKeyRange(): List[KeyRange] = {
    val samplePerWorker: Int = sample.length / numberOfWorkers
    val groupedSample = sample.sorted.grouped(samplePerWorker).toList
    def acc(rawRanges: List[List[Key]], start: Key): List[KeyRange] = {
      rawRanges match {
        case Nil => List()
        case a :: Nil => List(new KeyRange(start, Key.max))
        case h :: t => new KeyRange(start, h.last) :: acc(t, next(h.last))
      }
    }
    acc(groupedSample, Key.min)
  }

  def requestShuffling(): Unit = {
    val responses = clients.zip(stubs).toSeq.map {
      case (client, stub) => {
        val promise = Promise[Unit]()

        val request = ShuffleRunRequest()
        stub.shuffleData(request).onComplete {
          case Success(response) => {
            if (response.isShufflingSuccessful) {
              promise.success(())
            } else {
              promise.failure(new Exception("Shuffling failed"))
            }
          }
          case Failure(e) => promise.failure(e)
        }

        promise.future
      }
    }

    try {
      Await.result(Future.sequence(responses), Duration.Inf)
      state = MasterReceivedShuffleResponse
    } catch {
      case e: Exception => {
        state = MasterReceivedShuffleResponseFailure
        logger.info(s"Failed to receive shuffle data: ${e.getMessage}")
      }
    }
  }
  
}

class ServerImpl(clients: ListBuffer[Node]) extends MasterServiceGrpc.MasterService with Logging {

  override def establishConnection(request: EstablishRequest): Future[EstablishResponse] = {

    val node = new Node(request.workerIp, request.workerPort)

    // logger.info(s"[Master] Worker ${request.workerIp}:${request.workerPort} connected")
    logger.info(s"[Master] Worker ${request.workerIp}:${request.workerPort} connected")
    clients.synchronized {
      clients += node
    }

    val response = EstablishResponse(isEstablishmentSuccessful = true)
    Future.successful(response)
  }
}
