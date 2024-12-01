import Core.Block
import Core.Block._
import Core.Key._
import Core.Record.convertFromString

import Network.NetworkClient
import Network.Network.{IPAddr, Port}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}
import scala.io.Source

object Worker {
  def main(args: Array[String]): Unit = {
    val network = new NetworkClient

    try {
      network.connect_to_server()
      // network.sendRecords()
      network.send_unmatched_data()
      network.wait_until_all_data_received()
    } catch {
      case except: Exception => println(except)
    } finally {
      network.shutdown()
    }

  }
}
