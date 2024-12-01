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
      network.send_sample()
      network.send_unmatched_data()
      network.wait_until_all_data_received()
    } catch {
      case except: Exception => println(except)
    } finally {
      network.shutdown()
    }

  }

  private val ip: IPAddr = ???
  private val port: Port = ???
  private val inputDirs: List[String] = ???
  private val outputDir: String = ???
  private lazy val blocks: List[Block] = inputDirs.map(makeBlockFromFile(_))

  // TODO get SamplingRequest
  def sampling(size: Int): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val f = Future { blocks map (_.sampling(size)) }

    f.onComplete({
      case Success(value) => value
      case Failure(exception) => exception
    })

    // TODO send to Master
  }
  // TODO send SampleResponse

  // TODO get ShuffleRequest
  // def shuffling(start: Key, end: Key): Partition = {}
  // TODO send ShuffleResponse
}
