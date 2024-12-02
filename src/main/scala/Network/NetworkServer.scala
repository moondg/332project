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

// Import protobuf messages and services
import message.establishment.{EstablishRequest, EstablishResponse}
import message.sampling.{SampleRequest, SampleResponse}
import message.partitioning.{PartitionRequest, PartitionResponse}
import message.shuffling.{ShuffleRunRequest, ShuffleRunResponse, ShuffleExchangeRequest, ShuffleExchangeResponse}
import message.merging.{MergeRequest, MergeResponse}
import message.verification.{VerificationRequest, VerificationResponse}
import message.service.{MasterServiceGrpc, WorkerServiceGrpc}
import message.common.{DataChunk, KeyRange, KeyRangeTableRow, KeyRangeTable}

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
  var clients: ListBuffer[WorkerStatus] = ListBuffer.empty
  var sample: Array[Byte] = Array.emptyByteArray

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
        .forAddress(client.ip, client.port)
        .usePlaintext()
        .build()
    }.toSeq
    stubs = channels.map { channel =>
      WorkerServiceGrpc.stub(channel)
    }
  }

  def requestSampling(): Unit = {
        
    // Perform this to each worker
    val responses: Seq[Future[List[Byte]]] = clients.zip(stubs).toSeq.map {
        case (client, stub) => {
            val request = SampleRequest(
                workerIp=client.ip, 
                workerPort=client.port,
                percentageOfSampling=1
            )
            
            val promise = Promise[List[Byte]]()
            val buffer = ListBuffer.empty[Byte]
            var haveReachedEOF = false

            // Define the response observer to handle the response from the worker
            val responseObserver = new StreamObserver[SampleResponse] {
                override def onNext(value: SampleResponse): Unit = {
                   value.sample match {
                    case Some(datachunk) =>
                        haveReachedEOF = datachunk.isEOF
                        buffer ++= datachunk.data.toByteArray
                        println(s"data received: ${datachunk.data.toByteArray}")
                    case None =>
                        onError(new Exception("Received empty data chunk"))
                    }
                }

                override def onError(t: Throwable): Unit = {
                    promise.failure(t)
                }

                override def onCompleted(): Unit = {
                    if (haveReachedEOF) {
                        promise.success(buffer.toList)
                    } else {
                        promise.failure(new Exception("Did not receive EOF"))
                    }
                }
                
                // Concurrently run the sampleData request
                def receivedResponse: Future[List[Byte]] = promise.future
            }
            
            // Request sample data for current worker 
            stub.sampleData(request, responseObserver)
            responseObserver.receivedResponse
        }
    }


    Future.sequence(responses).map { allResponses =>
      sample = allResponses.flatten.toArray
    }.onComplete {
        case Success(_) => {
            println("Sample data received successfully")
        }
        case Failure(e) => {
            println("Sample data received failure")
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
  /*
  def ipLogging(): Unit = {
    @tailrec
    def clientIPLogging(clients: List[WorkerStatus]): Unit = {
      assert(clients != Nil)
      logger.info(s"[Master] Worker IP - ${clients.head.ip}")
      if (clients.tail != Nil) clientIPLogging(clients.tail)
    }
    val ip = InetAddress.getLocalHost.getAddress
    logger.info(
      s"[Master] Master IP:Port - ${ip(0).toString}.${ip(1).toString}.${ip(2).toString}.${ip(3).toString}:${port.toString}")
    clientIPLogging(clients.toList)
  }
  */
  /*
  def divideKeyRange(): Unit = {
    val sampleCountPerWorker: Int = sample.length / numberOfWorkers
    @tailrec
    def divideKeyRangeRecur(
        data: List[Key],
        dataCount: Int,
        whoseRange: Int,
        rangeHead: Key): Unit = {
      if (whoseRange == numberOfWorkers - 1) {
        // (head, MAXIMUM)
        clients(numberOfWorkers - 1).keyRange = (rangeHead, 0xff.toChar.toString * 10)
      } else if (dataCount == 1) {
        clients(whoseRange).keyRange = (rangeHead, data.head)
        divideKeyRangeRecur(data.tail, sampleCountPerWorker, whoseRange + 1, data.head + 1)
      } else {
        divideKeyRangeRecur(data.tail, dataCount - 1, whoseRange, rangeHead)
      }
    }
    // rangeHead = MINIMUM
    divideKeyRangeRecur(sample.sorted.toList, sampleCountPerWorker, 0, 0x00.toChar.toString * 10)
  }
  */
}

class ServerImpl(clients: ListBuffer[WorkerStatus]) extends MasterServiceGrpc.MasterService with Logging {

  override def establishConnection(request: EstablishRequest): Future[EstablishResponse] = {

    val workerStatus = new WorkerStatus(request.workerIp, request.workerPort)

    // logger.info(s"[Master] Worker ${request.workerIp}:${request.workerPort} connected")
    println(s"[Master] Worker ${request.workerIp}:${request.workerPort} connected")
    clients.synchronized {
      clients += workerStatus
    }

    val response = EstablishResponse(isEstablishmentSuccessful = true)
    Future.successful(response)
  }
}