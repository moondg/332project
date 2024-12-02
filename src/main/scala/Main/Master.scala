import Network.NetworkServer
import scala.concurrent.ExecutionContext

object Master {
  def main(args: Array[String]): Unit = {
    val argsFormat = "master [number of workers] [master network port (not essential)]"

    require(args.length == 1 || args.length == 2, argsFormat)
    val numberOfWorkers = args(0).toInt
    val port = if (args.length == 1) 50051 else args(1).toInt

    val network = new NetworkServer(
      port, 
      numberOfWorkers, 
      executionContext = ExecutionContext.global
      )

    try {
      network.startServer()
      // network.ongoingServer()

      // Block until network.clientList's length is equal to numberOfWorkers
      while (network.clientList.length < numberOfWorkers) {
        println("Waiting for workers to connect")
        Thread.sleep(1000)
      }


    } catch {
      case except: Exception => println(except)
    } finally {
      network.stopServer()
    }
  }
}
