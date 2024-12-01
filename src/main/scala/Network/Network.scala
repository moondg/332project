package Network

import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.Map
import scala.util.{Success, Failure}
import scala.annotation.tailrec

import java.util.concurrent.TimeUnit

import Common._
import Core._
import Core.Table._
import Core.Key._
import Core.Block._

import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
import io.grpc.stub.StreamObserver;

import message.gRPCtest.{ConnectionGrpc, TestRequest, TestResponse}

object Network {
  type IPAddr = String
  type Port = Int
  type Node = (IPAddr, Port)
}

import Network._
import java.net.InetAddress

class NetworkServer(port: Int, numberOfWorkers: Int, executionContext: ExecutionContext) {

  var server: Server = null
  var state: MasterState = MasterInitial
  val clients: List[WorkerStatus] = ???

  def startServer(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(ConnectionGrpc.bindService(new ServerImpl, executionContext))
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
    val msgType = msg.messasgeType
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

}

class ServerImpl extends ConnectionGrpc.Connection {
  override def testMethod(
      request: message.gRPCtest.TestRequest): Future[message.gRPCtest.TestResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.TestResponse(reply = "Your response message")
    Future.successful(response)
  }

  override def establishConnection(
      request: message.gRPCtest.ConnectionRequest): Future[message.gRPCtest.ConnectionResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.ConnectionResponse(reply = "Your response message")
    Future.successful(response)
  }
}

class NetworkClient(masterIP: String, masterPort: Int, val inputDirs: List[String], val outputDir: String) {
  val ip: IPAddr = ???

  lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))
  val master: Node = (masterIP, masterPort)

  var state: WorkerState = WorkerInitial
  var server: Server = null

  val channel = ManagedChannelBuilder
    .forAddress(masterIP, masterPort) // Connect to master
    .usePlaintext() // No encryption
    .build() // Build the channel

  // Create a stubs
  val blockingStub = ConnectionGrpc.blockingStub(channel)
  val nonBlockingStub = ConnectionGrpc.stub(channel)

  def connectToServer(): Unit = {
    val request = new EstablishRequest(ip, port)
    val response =  
    state = WorkerSentSampleResponse
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
          sendRecords(sending, node)
          sendPartition(remaining, next)
        }
      }
    }
  }

  def send_unmatched_data(): Unit = {}

  def wait_until_all_data_received(): Unit = {}
}

class ClientImpl extends ConnectionGrpc.Connection {
  override def testMethod(
      request: message.gRPCtest.TestRequest): Future[message.gRPCtest.TestResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.TestResponse(reply = "Your response message")
    Future.successful(response)
  }

  override def establishConnection(
      request: message.gRPCtest.ConnectionRequest): Future[message.gRPCtest.ConnectionResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.ConnectionResponse(reply = "Your response message")
    Future.successful(response)
  }
}