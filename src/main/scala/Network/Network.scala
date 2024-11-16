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

class NetworkServer(port: Int, executionContext: ExecutionContext) {

  var server: Server = null
  var state: MasterState = MASTER_INITIAL
  val clients = ???

  def start_server(): Unit = {
    server = ServerBuilder.forport(port)
      .addService(ConnectionGrpc.bindService(new ServerImpl, executionContext))
      .build().start()
  }

  def ongoing_server(): Unit = {
    if(server != null){

    }
  }

  def stop_server(): Unit = {
    if(server != null){
      server.shutdown.awaitTermination(1, TimeUnit.SECONDS)
    }
  }

  def send_msg_to_client(msg: Message): Unit = {

  }

  def pivot_check(): Unit = {

    val f = Future {

    }

    f.onComplete {
      case Success(v) => {

      }
      case Failure(e) => {

      }
    }

  }


}

class ServerImpl extends ConnectionGrpc.Connection {

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