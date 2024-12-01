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

class NetworkServer(port: Int, numberOfWorkers: Int, executionContext: ExecutionContext) {

  var server: Server = null
  var state: MasterState = MasterInitial
  var samples: Array[String] = Array.empty[String]
  var clients: Array[WorkerStatus] = Array.empty[WorkerStatus]

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
          divideKeyRange()
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

  def send_msg(msg: Message): Unit = {}

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

  def divideKeyRange(): Unit = {
    @tailrec
    def divideKeyRangeRec(data: List[String], leftWorker: Int, checkedData: Int, perWorker: Int, keyHead: String): Unit = {
      assert(leftWorker > 0 && data.nonEmpty)
      if(leftWorker == 1) {
        clients(clients.length - 1).keyRange = (data.head, data.last)
      }
      else {
        if(checkedData == 0) {
          divideKeyRangeRec(data.tail, leftWorker, 1, perWorker, data.head)
        }
        else if(checkedData == perWorker) {
          clients(clients.length - leftWorker).keyRange = (keyHead, data.head)
          divideKeyRangeRec(data.tail, leftWorker-1, 0, perWorker, "")
        }
        else {
          divideKeyRangeRec(data.tail, leftWorker, checkedData + 1, perWorker, keyHead)
        }
      }
    }
    divideKeyRangeRec(samples.sorted.toList, numberOfWorkers, numberOfWorkers/samples.length, numberOfWorkers/samples.length, "")
  }
}

class ServerImpl extends ConnectionGrpc.Connection {
  override def testMethod(
      request: message.gRPCtest.TestRequest): Future[message.gRPCtest.TestResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.TestResponse(reply = "Your response message")
    Future.successful(response)
  }
}

class NetworkClient(masterIP: String, masterPort: Int, val inputDirs: List[String], val outputDir: String) {
  val ip: IPAddr = ???
  val port: Port = ???
  lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))
  val master: Node = (masterIP, masterPort)

  var state: WorkerState = WorkerInitial
  var server: Server = null

  def connect_to_server(): Unit = {
    val respond = ???
    state = WorkerSendedSample
  }

  def send_msg(msg: Message): Unit = {}

  def shutdown(): Unit = {
    state = WorkerDone
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
