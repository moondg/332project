import Network.NetworkClient
import Record._
import Key._

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

  private lazy val partition: Partition = ??? // TODO

  // TODO get SamplingRequest
  def sampling(size: Int): Stream[Key] = {
    partition.sampling(size).map(convertFromRecord(_))
  }
  // TODO send SampleResponse

  // TODO get ShuffleRequest
  def shuffling(start: Key, end: Key): Partition = {
    partition.shuffling(start, end)
  }
  // TODO send ShuffleResponse
}
