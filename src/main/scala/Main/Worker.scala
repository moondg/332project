import Core.Block
import Core.Block._
import Core.Key._
import Core.Record._
import Network.NetworkClient
import Network.Network.{IPAddr, Port, Node}

import java.net.InetAddress

import Common._

import org.apache.logging.log4j.scala.Logging

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.io.Source
import scala.concurrent.ExecutionContext

import Utils.Prelude._
import Utils.Postlude._

object Worker extends Logging {
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

    var workerFSM: MutableWorkerFSM = new MutableWorkerFSM(WorkerInitial)

    val network = new NetworkClient(
      master,
      client,
      inputDirs,
      outputDir,
      workerFSM,
      executionContext = ExecutionContext.global)

    try {
      network.start()
      network.connectToServer()

      while (network.isSendingDataComplete() == false || network.isMergeComplete() == false) {
        Thread.sleep(1000)
      }

      workerFSM.transition(WorkerEventFinishSorting)
      // assert(workerFSM.getState() == WorkerFinished)
      logger.info("[Worker] Finished sorting")

    } catch {
      case except: Exception => {
        workerFSM.transition(WorkerEventError)
        println(except)
      }
    } finally {
      network.shutdown()
      clearFile(outputDir)
    }

  }
}
