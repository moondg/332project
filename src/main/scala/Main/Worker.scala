import Core.Block
import Core.Block._
import Core.Key._
import Core.Record.convertFromString
import Network.NetworkClient
import Network.Network.{IPAddr, Port}

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
    val master = (masterNetwork(0): IPAddr, masterNetwork(1).toInt: Port)

    val ip = getIPAddr
    val port: Port = new java.net.ServerSocket(0).getLocalPort

    val inputDirs: List[String] = inputDirParse(args.toList)
    val outputDir: String = args(args.length - 1)

    val network = new NetworkClient(
      master,
      (ip, port),
      inputDirs,
      outputDir,
      executionContext = ExecutionContext.global)

    try {
      network.connectToServer()
      // network.sendRecords()
      network.send_unmatched_data()
      network.wait_until_all_data_received()
    } catch {
      case except: Exception => println(except)
    } finally {
      network.shutdown()
    }

  }

  def inputDirParse(args: List[String]): List[String] = {
    @tailrec
    def inputDirParseRecur(
        args: List[String],
        dir: List[String],
        isInputDir: Boolean): List[String] = {
      args match {
        case "-I" :: tail => inputDirParseRecur(tail, dir, true)
        case "-O" :: _ => dir
        case head :: tail =>
          if (isInputDir) inputDirParseRecur(tail, head :: dir, isInputDir)
          else inputDirParseRecur(tail, dir, isInputDir)
      }
    }
    inputDirParseRecur(args, Nil, false)
  }

}
