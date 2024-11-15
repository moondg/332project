package Network

import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import Common._

import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
import io.grpc.stub.StreamObserver;

class NetworkServer {

  var server: Server = null
  var state: MasterState = MASTER_INITIAL
  val clients = ???

  def start_server(): Unit = {
    server = ???
  }

  def ongoing_server(): Unit = {
    if(server != null){

    }
  }

  def stop_server(): Unit = {
    if(server != null){

    }
  }

  def send_msg_to_client(msg: Message): Unit = {

  }

  def pivot_check(): Unit = {

    val f = Future {

    }

    f.onComplete {

    }

  }


}

class NetworkClient {

  var client_id: Int = -1
  var state: WorkerState = WORKER_INITIAL

  def connect_to_server(): Unit = {
    val respond = ???
    client_id = ???
  }

  def send_msg_to_server(msg: Message): Unit = {

  }

  def shutdown(): Unit = {
    state = WORKER_DONE
  }
}