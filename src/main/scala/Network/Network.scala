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
  var clientList: ListBuffer[WorkerStatus] = ListBuffer.empty

  def startServer(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(MasterServiceGrpc.bindService(new ServerImpl(clientList), executionContext))
      .build()
      .start()
  }

  def ongoingServer(): Unit = {
    while (server != null && state != MasterFinished) {
      state match {
        case MasterReceivedSampleResponse => {
          pivot_check()
        }
        case MasterMakingPartition => {
          divide_part()
        }
        case MasterPendingMergeResponse => {}
      }
    }
  }

  def stopServer(): Unit = {
    if (server != null) {
      server.shutdown.awaitTermination(1, TimeUnit.SECONDS)
      state = MasterFinished
    }
  }

  def sendMsg(msg: Message): Unit = {
    val msgType = msg.msgType
    // msgType match {}
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

  def divide_part(): Unit = {}

  def ipLogging(): Unit = {
    @tailrec
    def clientIPLogging(clientList: List[WorkerStatus]): Unit = {
      assert(clientList != Nil)
      logger.info(s"Worker IP - ${clientList.head.ip}")
      if (clientList.tail != Nil) clientIPLogging(clientList.tail)
    }
    val ip = InetAddress.getLocalHost.getAddress
    logger.info(
      s"Master IP:Port - ${ip(0).toString}.${ip(1).toString}.${ip(2).toString}.${ip(3).toString}:${port.toString}")
    clientIPLogging(clientList.toList)
  }

}

class ServerImpl(clientList: ListBuffer[WorkerStatus]) extends MasterServiceGrpc.MasterService {

  override def establishConnection(request: EstablishRequest): Future[EstablishResponse] = {

    val workerStatus = new WorkerStatus(request.workerIp, request.workerPort)

    clientList.synchronized {
      clientList += workerStatus
    }

    val response = EstablishResponse(isEstablishmentSuccessful = true)
    Future.successful(response)
  }
}

class NetworkClient(
    val master: Node,
    val ip: IPAddr,
    val port: Port,
    val inputDirs: List[String],
    val outputDir: String,
    val executionContext: ExecutionContext)
    extends Logging {
  lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))

  var state: WorkerState = WorkerInitial
  var clientService: ClientImpl = null
  var server: Server = null

  val channelToMaster = ManagedChannelBuilder
    .forAddress(master._1, master._2)
    .usePlaintext()
    .build()

  val stubToMaster = MasterServiceGrpc.stub(channelToMaster)

  def startServer(): Unit = {
    clientService = new ClientImpl()
    server = ServerBuilder
      .forPort(port)
      .addService(WorkerServiceGrpc.bindService(clientService, executionContext))
      .build()
      .start()
  }

  def connectToServer(): Unit = {
    logger.info("[Worker] Trying to establish connection to master")

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

  def send_msg(msg: Message): Unit = {}

  def shutdown(): Unit = {
    state = WorkerFinished
  }

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

  override def sampleData(request: SampleRequest): Future[SampleResponse] = {
    val repeatedSampleDataChunks = Seq(???) // TODO: Sample Datas and send it to master

    val response = SampleResponse(isSamplingSuccessful = true, samples = repeatedSampleDataChunks)
    Future.successful(response)
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
