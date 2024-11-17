import Network.NetworkClient

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

  lazy val partition = ??? // TODO
  def sampling(size: Int): Stream[Key] = {
    partition.sampling(size).map(_.getKey)
  }
}
