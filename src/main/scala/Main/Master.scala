import Network.NetworkServer
import scala.concurrent.ExecutionContext

object Master {
  def main(args: Array[String]): Unit = {
    val network = new NetworkServer(port = 50051, executionContext = ExecutionContext.global)

    try {
      network.startServer()
      network.ongoingServer()
    } catch {
      case except: Exception => println(except)
    } finally {
      network.stopServer()
    }
  }
}
