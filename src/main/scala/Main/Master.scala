import Network.NetworkServer

object Master {
  def main(args: Array[String]): Unit = {
    val network = new NetworkServer

    try {
      network.start_server()
      network.ongoing_server()
    }
    catch {
      case except: Exception => println(except)
    }
    finally {
      network.stop_server()
    }
  }
}