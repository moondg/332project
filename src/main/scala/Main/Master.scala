import Network.NetworkServer
import scala.concurrent.ExecutionContext
import Core.Table._
import org.apache.logging.log4j.scala.Logging

object Master extends Logging {
  def main(args: Array[String]): Unit = {
    // Parse Arguments
    val argsFormat = "master [number of workers] [master network port (not essential)]"

    require(args.length == 1 || args.length == 2, argsFormat)
    val numberOfWorkers = args(0).toInt
    val port = if (args.length == 1) 50075 else args(1).toInt

    // Run NetworkServer
    val networkServer =
      new NetworkServer(port, numberOfWorkers, executionContext = ExecutionContext.global)

    try {
      networkServer.start()

      logger.info("[Master] Waiting for workers to connect")
      // Block until networkServer.clientList's length is equal to numberOfWorkers
      while (networkServer.clients.length < numberOfWorkers) { Thread.sleep(1000) }

      logger.info("[Master] All workers connected")
      networkServer.ipLogging()
      networkServer.createChannels()

      // Sampling Phase
      logger.info("[Master] Sampling Phase")
      networkServer.requestSampling()

      // Partitioning Phase
      logger.info("[Master] Partitioning Phase")
      logger.info("[Master] Sending partitioning request")
      networkServer.requestPartitioning()

    } catch {
      case except: Exception => logger.error(except)
    } finally {
      networkServer.stop()
    }
  }
}
