import Network.NetworkServer
import scala.concurrent.ExecutionContext

object Master {
  def main(args: Array[String]): Unit = {
    val network = new NetworkServer(port = 50051, executionContext = ExecutionContext.global)

    try {
      network.start_server()
      network.ongoing_server()
    } catch {
      case except: Exception => println(except)
    } finally {
      network.stop_server()
    }
  }
}
