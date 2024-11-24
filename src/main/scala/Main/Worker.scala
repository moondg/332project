import Network.NetworkClient
import Record._
import Key._

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

  private lazy val partition: Partition = ??? // TODO
  def sampling(size: Int): Stream[Key] = {
    partition.sampling(size).map(convertFromRecord(_))
  }
}
