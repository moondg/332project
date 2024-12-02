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
import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
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
import message.common.{DataChunk, KeyRange, KeyRangeTableRow, KeyRangeTable}
import javax.xml.crypto.Data

class NetworkClient(
    val master: Node,
    val client: Node,
    val inputDirs: List[String],
    val outputDir: String,
    val executionContext: ExecutionContext)
    extends Logging {

  val (ip, port) = client
  lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))

  var clientService: ClientImpl = null
  var server: Server = null

  val channelToMaster = ManagedChannelBuilder
    .forAddress(master._1, master._2)
    .usePlaintext()
    .build()

  val stubToMaster = MasterServiceGrpc.stub(channelToMaster)

  def start(): Unit = {
    clientService = new ClientImpl()
    server = ServerBuilder
      .forPort(port)
      .addService(WorkerServiceGrpc.bindService(clientService, executionContext))
      .build()
      .start()
  }

  def connectToServer(): Unit = {
    println("[Worker] Trying to establish connection to master")

    // Create a request to establish connection
    val request = new EstablishRequest(workerIp = ip, workerPort = port)

    // Send the request to master
    val response: Future[EstablishResponse] = stubToMaster.establishConnection(request)

    try {
      // Wait for the response
      val result = Await.result(response, 1.hour)
      if (result.isEstablishmentSuccessful) {
        println("[Worker] Connection established")
      } else {
        println("[Worker] Connection failed")
      }

    } catch {
      case e: Exception => println(e)
    }
  }

  def shutdown(): Unit = {}

  def sendSamples(sample: List[Key], node: Node): Unit = {}
  def sendRecords(records: List[Record], node: Node): Unit = {}

  // TODO get SamplingRequest
  def sampling(size: Int): Unit = {
    val f = Future { blocks map (_.sampling(size)) }

    f.onComplete({
      case Success(samples) => samples.map(sendSamples(_, master))
      case Failure(exception) => exception
    })
  }

  // TODO send SampleResponse
  def partitioning(table: Table): Unit = {
    blocks.map(block => sendPartition(block.block.sorted, table))
    // The reason for tailrec inside function, see Tim's answer from
    // https://stackoverflow.com/questions/4785502/why-wont-the-scala-compiler-apply-tail-call-optimization-unless-a-method-is-fin
    @tailrec
    def sendPartition(records: List[Record], table: Table): Unit = {
      table match {
        case Nil => ()
        case head :: next => {
          val (keyRange, node) = head
          val (sending, remaining) = records span (keyRange.contains(_))
          logger.info("[Worker] Partition ${ip} -> ${node.ip} Start")
          sendRecords(sending, node)
          logger.info("[Worker] Partition ${ip} -> ${node.ip} Done")
          sendPartition(remaining, next)
        }
      }
    }
  }

  def send_unmatched_data(): Unit = {}

  def wait_until_all_data_received(): Unit = {}
}

class ClientImpl extends WorkerServiceGrpc.WorkerService {

  override def sampleData(
    request: SampleRequest,
    responseObserver: StreamObserver[SampleResponse]
  ): Unit = {
    
    val percentageOfSampling = request.percentageOfSampling

    // TODO: Sample Data here

    (1 to 10000).foreach { i =>
      val response = SampleResponse(
        isSamplingSuccessful = true, 
        // Option[message.common.DataChunk]
        sample=Some(
            DataChunk(
                data = ByteString.copyFrom(java.nio.ByteBuffer.allocate(4).putInt(i).array()),
                chunkIndex = i,
                isEOF = (i == 10000)
            )
        )
      )
      responseObserver.onNext(response)
    }
    responseObserver.onCompleted()
  }

  override def partitionData(request: PartitionRequest): Future[PartitionResponse] = {
    val keyRangeTable = request.table

    // TODO: Perform Partitioning here
    val response = PartitionResponse(isPartitioningSuccessful = true)
    Future.successful(response)
  }

  override def runShuffle(request: ShuffleRunRequest): Future[ShuffleRunResponse] = {

    val response = ShuffleRunResponse(isShufflingSuccessful = true)
    Future.successful(response)
  }

  override def exchangeData(request: ShuffleExchangeRequest): Future[ShuffleExchangeResponse] = {

    // Pack values and send data
    val data = Seq(???)

    val response = ShuffleExchangeResponse(
      sourceIp = "",
      sourcePort = 0,
      destinationIp = "",
      destinationPort = 0,
      data = data)
    Future.successful(response)
  }

  override def mergeData(request: MergeRequest): Future[MergeResponse] = {
    val response = MergeResponse(isMergeSuccessful = true)
    Future.successful(response)
  }

  override def verifyKeyRange(request: VerificationRequest): Future[VerificationResponse] = {
    val response = VerificationResponse(isVerificationSuccessful = true)
    Future.successful(response)
  }
}
