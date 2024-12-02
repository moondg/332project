import Network.NetworkServer
import scala.concurrent.ExecutionContext

object Master {
  def main(args: Array[String]): Unit = {
    // Parse Arguments
    val argsFormat = "master [number of workers] [master network port (not essential)]"

    require(args.length == 1 || args.length == 2, argsFormat)
    val numberOfWorkers = args(0).toInt
    val port = if (args.length == 1) 50051 else args(1).toInt

    // Run NetworkServer
    val networkServer = new NetworkServer(
      port, 
      numberOfWorkers, 
      executionContext = ExecutionContext.global
      )

    try {
      networkServer.start()
      
      println("Waiting for workers to connect")
      // Block until networkServer.clientList's length is equal to numberOfWorkers
      while (networkServer.clientList.length < numberOfWorkers) { Thread.sleep(1000) }


    } catch {
      case except: Exception => println(except)
    } finally {
      networkServer.stop()
    }
  }
}
