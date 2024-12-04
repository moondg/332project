import Core.Block
import Core.Block._
import Core.Key._
import Core.Record._
import Network.NetworkClient
import Network.Network.{IPAddr, Port, Node}

import java.net.InetAddress

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.io.Source
import scala.concurrent.ExecutionContext

import Utils.Prelude._

object Worker {
  def main(args: Array[String]): Unit = {
    val argsFormat =
      "worker [master IP:port] -I [input directory] [input directory] … [input directory] -O [output directory]"

    require(
      args.length >= 5 &&
        args(0).contains(":") &&
        args(1) == "-I" &&
        args(args.length - 2) == "-O",
      argsFormat)

    val masterNetwork = args(0).split(":")
    val master: Node = (masterNetwork(0), masterNetwork(1).toInt)
    val client: Node = (getIPAddr(), getPort())
    val inputDirs: List[String] = inputDirParse(args.toList)
    val outputDir: String = args.last

    val network = new NetworkClient(
      master,
      client,
      inputDirs,
      outputDir,
      executionContext = ExecutionContext.global)

    try {
      network.start()
      network.connectToServer()
      while (true) {
        Thread.sleep(1000)
      }
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
