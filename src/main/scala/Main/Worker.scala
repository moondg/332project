import Network.NetworkClient

import Core.Block
import Core.Key._

object Worker {
  def main(args: Array[String]): Unit = {
    val network = new NetworkClient

    try {
      network.connect_to_server()

    } catch {
      case except: Exception => println(except)
    } finally {
      network.shutdown()
    }

  }

  private lazy val block: Block = ??? // TODO

  // TODO get SamplingRequest
  def sampling(size: Int): Stream[Key] = {
    block.sampling(size).map(_.key)
  }
  // TODO send SampleResponse

  // TODO get ShuffleRequest
  // def shuffling(start: Key, end: Key): Partition = {}
  // TODO send ShuffleResponse
}
