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

object Worker {
  def main(args: Array[String]): Unit = {
    val argsFormat = "worker [master IP:port] -I [input directory] [input directory] … [input directory] -O [output directory]"
    require(args.length >= 5 &&
      args(0).contains(":") &&
      args(1) == "-I" &&
      args(args.length - 2) == "-O"
      , argsFormat)

    val masterNetwork = args(0).split(":")
    val masterIP: IPAddr = masterNetwork(0)
    val masterPort: Port = masterNetwork(1).toInt

    val ip: IPAddr = ???
    val port: Port = ???

    val inputDirs: List[String] = inputDirParse(args.toList)
    val outputDir: String = args(args.length - 1)

    lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))

    val network = new NetworkClient(masterIP, masterPort)

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

  def inputDirParse(args: List[String]): List[String] = {
    @tailrec
    def inputDirParseRecur(args: List[String], dir: List[String], isInputDir: Boolean): List[String] = {
      args match {
        case "-I"::tail => inputDirParseRecur(tail, dir, true)
        case "-O"::_ => dir
        case head::tail =>
          if(isInputDir) inputDirParseRecur(tail, head::dir, isInputDir)
          else inputDirParseRecur(tail, dir, isInputDir)
      }
    }
    inputDirParseRecur(args, Nil, false)
  }

}
