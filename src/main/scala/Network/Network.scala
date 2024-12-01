package Network

import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.Map
import scala.util.{Success, Failure}

import java.util.concurrent.TimeUnit

import Common._

import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
import io.grpc.stub.StreamObserver;

import message.gRPCtest.{ConnectionGrpc, TestRequest, TestResponse}

object Network {
  type IPAddr = String
  type Port = Int
}

class NetworkServer(port: Int, executionContext: ExecutionContext) {

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
  

  def divide_part(): Unit = {}

}

class ServerImpl extends ConnectionGrpc.Connection {
  override def testMethod(
      request: message.gRPCtest.TestRequest): Future[message.gRPCtest.TestResponse] = {
    // Implement your logic here
    val response = message.gRPCtest.TestResponse(reply = "Your response message")
    Future.successful(response)
  }
}

class NetworkClient {

  var client_id: Int = -1
  var state: WorkerState = WorkerInitial
  var server: Server = null

  def connect_to_server(): Unit = {
    val respond = ???
    client_id = ???
    state = WorkerSentSampleResponse
  }

  def send_msg(msg: Message): Unit = {}

  def shutdown(): Unit = {
    state = WorkerFinished
  }

  def send_sample(): Unit = {}

  def send_unmatched_data(): Unit = {}

  def wait_until_all_data_received(): Unit = {}
}