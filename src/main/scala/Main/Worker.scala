import Core.Block
import Core.Block._
import Core.Key._
import Core.Record.convertFromString
import Network.NetworkClient
import Network.Network.{IPAddr, Port}

import java.net.InetAddress

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.io.Source
import scala.concurrent.ExecutionContext

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
    val masterIP: IPAddr = masterNetwork(0)
    val masterPort: Port = masterNetwork(1).toInt

    val ip: Array[Byte] = InetAddress.getLocalHost.getAddress
    val ipString: String =
      ip(0).toString + "." + ip(1).toString + "." + ip(2).toString + "." + ip(3).toString
    val serverSocket = new java.net.ServerSocket(0)
    val port: Port = serverSocket.getLocalPort

    val inputDirs: List[String] = inputDirParse(args.toList)
    val outputDir: String = args(args.length - 1)

    lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))

    val network = new NetworkClient(
      (masterIP, masterPort),
      (ipString, port),
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
