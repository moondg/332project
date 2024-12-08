import Network.NetworkServer
import scala.concurrent.ExecutionContext
import Core.Table._
import Common._
import org.apache.logging.log4j.scala.Logging

object Master extends Logging {
  def main(args: Array[String]): Unit = {
    // Parse Arguments
    val argsFormat = "master [number of workers] [master network port (not essential)]"

    require(args.length == 1 || args.length == 2, argsFormat)
    val numberOfWorkers = args(0).toInt
    val port = if (args.length == 1) 50075 else args(1).toInt

    var masterFSM: MutableMasterFSM = new MutableMasterFSM(MasterInitial)

    // Run NetworkServer
    val networkServer =
      new NetworkServer(
        port,
        numberOfWorkers,
        masterFSM,
        executionContext = ExecutionContext.global)

    try {
      networkServer.start()

      logger.info("[Master] Waiting for workers to connect")
      // Block until networkServer.clientList's length is equal to numberOfWorkers
      while (networkServer.clients.length < numberOfWorkers) { Thread.sleep(1000) }

      masterFSM.transition(MasterEventConnectionEstablished)
      // assert(masterFSM.getState() == MasterConnectionEstablished)
      logger.info("[Master] All workers connected")

      networkServer.ipLogging()
      networkServer.createChannels()

      // Sampling Phase
      masterFSM.transition(MasterEventProceedSampling)
      // assert(masterFSM.getState() == MasterSendingSampleRequest)
      logger.info("[Master] Sampling Phase")
      logger.info("[Master] Sending sampling request")
      networkServer.requestSampling()
      // assert(masterFSM.getState() == MasterReceivedSampleResponse)

      // Partitioning Phase
      masterFSM.transition(MasterEventMakePartition)
      // assert(masterFSM.getState() == MasterMakingPartition)
      logger.info("[Master] Partitioning Phase")
      logger.info("[Master] Sending partitioning request")
      networkServer.requestPartitioning()
      // assert(masterFSM.getState() == MasterReceivedPartitionResponse)

      // Shuffling Phase
      masterFSM.transition(MasterEventProceedShuffling)
      // assert(masterFSM.getState() == MasterSendingShuffleRequest)
      logger.info("[Master] Shuffling Phase")
      logger.info("[Master] Sending shuffling request")
      networkServer.requestShuffling()
      // assert(masterFSM.getState() == MasterReceivedShuffleResponse)

      // Merging Phase
      masterFSM.transition(MasterEventProceedMerging)
      // assert(masterFSM.getState() == MasterSendingMergeRequest)
      logger.info("[Master] Merging Phase")
      logger.info("[Master] Sending merging request")
      networkServer.requestMerging()
      // assert(masterFSM.getState() == MasterReceivedMergeResponse)

      // Verification Phase
      // logger.info("[Master] Verification Phase")
      // logger.info("[Master] Sending verification request")
      // networkServer.requestVerification()

      masterFSM.transition(MasterEventFinishSorting)
      // assert(masterFSM.getState() == MasterFinished)
      logger.info("[Master] Finished Sorting")

    } catch {
      case except: Exception => {
        masterFSM.transition(MasterEventError)
        logger.error(except)
      }
    } finally {
      networkServer.stop()
    }
  }
}
