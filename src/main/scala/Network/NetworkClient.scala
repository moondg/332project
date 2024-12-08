package Network

import Network._

import java.io.{File, FileOutputStream}
import java.util.logging.FileHandler
import scala.::
import scala.collection.mutable

// Import necessary scala libraries
import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.{Map, ListBuffer}
import scala.util.{Success, Failure}
import scala.annotation.tailrec
import scala.util.hashing.MurmurHash3.stringHash

// Import necessary java libraries
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.io.PrintWriter

// Import logging libraries
import org.apache.logging.log4j.scala.Logging

// Import necessary project libraries
import Common._
import Core._
import Core.Table._
import Core.Key._
import Core.{Key, KeyRange}
import Core.Block._
import Utils.Prelude._
import Utils.Interlude._
import Utils.Postlude._

// Import gRPC libraries
import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status, ManagedChannel}
import io.grpc.stub.StreamObserver
import com.google.protobuf.ByteString

// Import protobuf messages and services
import message.establishment.{EstablishRequest, EstablishResponse}
import message.sampling.{SampleRequest, SampleResponse}
import message.partitioning.{PartitionRequest, PartitionResponse}
import message.shuffling.{ShuffleRunRequest, ShuffleRunResponse, ShuffleExchangeRequest, ShuffleExchangeResponse}
import message.merging.{MergeRequest, MergeResponse}
import message.verification.{VerificationRequest, VerificationResponse}
import message.service.{MasterServiceGrpc, WorkerServiceGrpc}
import message.common.{DataChunk, KeyRange, KeyRangeTableRow, KeyRangeTable}
import javax.xml.crypto.Data
import Core.Constant.Size
import Core.Record.recordFrom
import Core.Constant.Prefix

class NetworkClient(
    master: Node,
    client: Node,
    inputDirs: List[String],
    outputDir: String,
    workerFSM: MutableWorkerFSM,
    executionContext: ExecutionContext)
    extends Logging {

  val (ip, port) = client

  var clientService: ClientImpl = null
  var server: Server = null

  val channelToMaster = ManagedChannelBuilder
    .forAddress(master._1, master._2)
    .usePlaintext()
    .build()

  val stubToMaster = MasterServiceGrpc.stub(channelToMaster)

  var channelToWorkers = List.empty[ManagedChannel]

  var stubToWorkers = List.empty[WorkerServiceGrpc.WorkerServiceStub]

  def start(): Unit = {
    clientService = new ClientImpl(inputDirs, outputDir, workerFSM, client)
    server = ServerBuilder
      .forPort(port)
      .addService(WorkerServiceGrpc.bindService(clientService, executionContext))
      .build()
      .start()
  }

  def connectToServer(): Unit = {
    logger.info("[Worker] Trying to establish connection to master")

    // Create a request to establish connection
    val request = new EstablishRequest(workerIp = ip, workerPort = port)

    // Send the request to master
    val response: Future[EstablishResponse] = stubToMaster.establishConnection(request)

    try {
      // Wait for the response
      val result = Await.result(response, 1.hour)
      if (result.isEstablishmentSuccessful) {
        workerFSM.transition(WorkerEventConnectionEstablished)
        assert(workerFSM.getState() == WorkerConnectionEstablished)
        logger.info("[Worker] Connection established")
        
      } else {
        workerFSM.transition(WorkerEventError)
        logger.info("[Worker] Connection failed")
      }

    } catch {
      case e: Exception => {
        workerFSM.transition(WorkerEventError)
        logger.error(e)
      }
    }
  }

  def shutdown(): Unit = {}

  def sendSamples(sample: List[Key], node: Node): Unit = {}
  def sendRecords(records: List[Record], node: Node): Unit = {}

  def isSendingDataComplete(): Boolean = {
    return clientService.sendCompleteCount == channelToWorkers.length
  }
}

class ClientImpl(
  inputDirs: List[String], 
  outputDir: String, 
  workerFSM: MutableWorkerFSM,
  thisClient: Node)
    extends WorkerServiceGrpc.WorkerService
    with Logging {

  val fileNames = inputDirs.map(getFileNames).flatten
  val filePaths = getAllFilePaths(inputDirs)

  var keyRangeTable: Table = null
  var sendCompleteCount: Int = 0

  override def sampleData(
      request: SampleRequest,
      responseObserver: StreamObserver[SampleResponse]): Unit = {
    
    assert(workerFSM.getState() == WorkerConnectionEstablished)
    workerFSM.transition(WorkerEventReceiveSampleRequest)
    assert (workerFSM.getState() == WorkerReceivedSampleRequest)
    logger.info("[Worker] Sample request received")

    val percentageOfSampling = request.percentageOfSampling

    var index = 0

    logger.info("[Worker] Start sending samples")
    workerFSM.transition(WorkerEventSendSampleResponse)
    assert(workerFSM.getState() == WorkerSendingSampleResponse)

    filePaths.foreach { filePath =>
      val sample = sampling(filePath, Constant.Sample.number)
      sample.foreach { key =>
        val dataChunk =
          DataChunk(data = ByteString.copyFrom(key.key), chunkIndex = index, isEOF = false)
        val response = SampleResponse(isSamplingSuccessful = true, sample = Some(dataChunk))
        responseObserver.onNext(response)
        index = index + 1
        if (index % 1000 == 0) logger.info(s"[Worker] Send ${index} samples")
      }
    }

    val emptyResponse =
      SampleResponse(isSamplingSuccessful = true, sample = Some(emptyDataChunk(index)))
    responseObserver.onNext(emptyResponse)
    responseObserver.onCompleted()
    
    workerFSM.transition(WorkerEventSendSampleResponseComplete)
    assert(workerFSM.getState() == WorkerSentSampleResponse)
    logger.info("[Worker] End sending samples")
    workerFSM.transition(WorkerEventProceedPartitioning)
    assert(workerFSM.getState() == WorkerProceedPartitioning)

  }

  override def partitionData(request: PartitionRequest): Future[PartitionResponse] = {

    assert(workerFSM.getState() == WorkerPendingPartitionRequest)

    workerFSM.transition(WorkerEventReceivePartitionRequest)
    assert(workerFSM.getState() == WorkerReceivedPartitionRequest)
    logger.info("[Worker] Partitioning request received")

    keyRangeTable = request.table match {
      case Some(keyRangeTableProto) =>
        keyRangeTableProto.rows.map { keyRangeProto =>
          val start = new Key(keyRangeProto.range match {
            case Some(range) => range.start.toByteArray
            case None => Array[Byte]()
          })

          val end = new Key(keyRangeProto.range match {
            case Some(range) => range.end.toByteArray
            case None => Array[Byte]()
          })

          val node = (keyRangeProto.ip, keyRangeProto.port)
          (new Core.KeyRange(start = start, end = end), node)
        }.toList
      case None =>
        logger.info("[Worker] No key range table provided")
        List.empty
    }
    keyRangeTable.foreach { case (keyRange, node) =>
      logger.info(s"Table: ${keyRange.hex} ${node._1}")
    }

    workerFSM.transition(WorkerEventPartitionData)
    assert (workerFSM.getState() == WorkerPartitioningData)

    val promise = Promise[PartitionResponse]()
    logger.info("[Worker] Partition Start")

    val processingFutures = (filePaths zip fileNames).map { case (filePath, fileName) =>
      Future {
        val block = blockFromFile(filePath)
        logger.info(s"[Worker] ${fileName} start")

        if (workerFSM.getState() != WorkerSending)

        try {
          for {
            (partition, node) <- dividePartition(block.block.sorted, keyRangeTable)
          } yield {
            var outFilePath = ""
            if (node == thisClient) {
              outFilePath =
                s"${outputDir}/${Prefix.shuffling}_${thisClient._1}_${stringHash(filePath)}"
            } else
              outFilePath =
                s"${outputDir}/${node._1}:${node._2}_${stringHash(filePath)}_${fileName}"
            writeFile(outFilePath, partition)
          }
          logger.info(s"[Worker] ${fileName} end")
          Success(PartitionResponse(isPartitioningSuccessful = true))
        } catch {
          case exception: Exception =>
            logger.error(s"[Worker] Error processing $fileName: ${exception.getMessage}")
            Failure(exception)
        }
      }
    }

    // Wait for all futures to complete and collect results
    Future.sequence(processingFutures).onComplete {
      case Success(results) =>
        if (results.forall(_.isSuccess)) {
          logger.info("[Worker] Partition Success")
          promise.success(PartitionResponse(isPartitioningSuccessful = true))
        } else {
          logger.info("[Worker] Partition Failed")
          val firstFailure = results.collectFirst { case Failure(e) => e }.get
          promise.failure(firstFailure)
        }
      case Failure(exception) =>
        promise.failure(exception)
    }

    promise.future
  }

  override def runShuffle(request: ShuffleRunRequest): Future[ShuffleRunResponse] = {

    assert(workerFSM.getState() == WorkerPendingShuffleRequest)

    workerFSM.transition(WorkerEventReceiveShuffleRequest)
    assert(workerFSM.getState() == WorkerReceivedShuffleRequest)

    // Get clients
    val clients =
      keyRangeTable.filter(tableRow => tableRow._2 != thisClient).map { tableRow => tableRow._2 }
    val channels = clients.map { case (ip, port) =>
      ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build()
    }
    val stubs = channels.map { channel => WorkerServiceGrpc.stub(channel) }

    workerFSM.transition(WorkerEventEstablishedInterworkerConnection)
    assert(workerFSM.getState() == WorkerWaitingAllDataReceived)

    // Send ExchangeRequest to all clients
    val exchangeResponses: Seq[Future[Boolean]] = clients.zip(stubs).map { case (client, stub) =>
      val promise = Promise[Boolean]()
      val exchangeRequest = ShuffleExchangeRequest(
        sourceIp = thisClient._1,
        sourcePort = thisClient._2,
        destinationIp = client._1,
        destinationPort = client._2)

      var haveReachedEOF = false

      var fileIdRef = 0
      var fileLength = 0
      val outFilePath = s"${outputDir}/${Prefix.shuffling}_${client._1}_"
      var file = new File(outFilePath + fileIdRef.toString)
      var fileWriter = new FileOutputStream(file, file.exists())

      val responseObserver = new StreamObserver[ShuffleExchangeResponse] {
        override def onNext(exchangeResponse: ShuffleExchangeResponse): Unit = {
          assert(client._1 == exchangeResponse.sourceIp)
          assert(client._2 == exchangeResponse.sourcePort)
          assert(thisClient._1 == exchangeResponse.destinationIp)
          assert(thisClient._2 == exchangeResponse.destinationPort)

          exchangeResponse.data match {
            case Some(dataChunk) => {
              val record = Record.recordFrom(dataChunk.data.toByteArray)
              haveReachedEOF = dataChunk.isEOF
              if (!haveReachedEOF) {
                fileLength += 1
                fileWriter.write(record.raw)
              } else {
                fileIdRef += 1
                fileWriter.close()
                if (fileLength == 0) file.delete()
                fileLength = 0
                file = new File(outFilePath + fileIdRef.toString)
                fileWriter = new FileOutputStream(file, file.exists())
              }
            }
            case None => {
              workerFSM.transition(WorkerEventError)
              onError(new Exception("Received empty data chunk"))
            }
          }
        }

        override def onError(t: Throwable): Unit = {
          workerFSM.transition(WorkerEventError)
          promise.failure(t)
        }

        override def onCompleted(): Unit = {
          if (haveReachedEOF) {
            fileWriter.close()
            file.delete()
            promise.success(true)
          } else {
            promise.failure(new Exception("Did not receive EOF"))
          }
        }
      }

      logger.info(s"[Worker] Sending exchange request to ${client._1}:${client._2}")

      stub.exchangeData(exchangeRequest, responseObserver)
      promise.future
    }

    val response: ShuffleRunResponse =
      try {
        // Await responses
        val allExchangeResponses = Await.result(Future.sequence(exchangeResponses), Duration.Inf)
        val isExchangeSuccessful = allExchangeResponses.forall(_ == true)
        workerFSM.transition(WorkerEventReceivedAllData)
        assert(workerFSM.getState() == WorkerReceivedAllData)
        logger.info(s"[Worker] Received data from other workers")

        workerFSM.transition(WorkerEventSendShuffleResponse)
        assert(workerFSM.getState() == WorkerSendingShuffleResponse)
        logger.info(s"[Worker] Contructing shuffle response")
        ShuffleRunResponse(isShufflingSuccessful = isExchangeSuccessful)
      } catch {
        case e: Exception =>
          logger.error(e)
          ShuffleRunResponse(isShufflingSuccessful = false)
      }

    // Close channels
    channels.foreach { channel => channel.shutdown() }

    workerFSM.transition(WorkerEventSendShuffleResponseComplete)
    assert(workerFSM.getState() == WorkerSentShuffleResponse)
    logger.info(s"[Worker] Finished Receiving data from other clients")

    // Proceed to merging
    workerFSM.transition(WorkerEventProceedMerging)
    assert(workerFSM.getState() == WorkerPendingMergeRequest)

    Future.successful(response)
  }

  override def exchangeData(
      request: ShuffleExchangeRequest,
      responseObserver: StreamObserver[ShuffleExchangeResponse]): Unit = {

    // Assert that the request is coming to the correct client
    assert(thisClient._1 == request.destinationIp)
    assert(thisClient._2 == request.destinationPort)

    logger.info(
      s"[Worker] Received exchange request from ${request.sourceIp}:${request.sourcePort}")
    
    var size = 0
    val exchangeFileNames = getFileNames(outputDir)
      .filter(s => (s takeWhile (_ != '_')) == s"${request.sourceIp}:${request.sourcePort}")

    exchangeFileNames.foreach { exchangeFileName =>
      val data =
        readFile(outputDir ++ "/" ++ exchangeFileName)
          .grouped(Constant.Size.record)
          .map(recordFrom)
          .zipWithIndex
          .foreach { case (record, index) =>
            val dataChunk =
              DataChunk(data = ByteString.copyFrom(record.raw), chunkIndex = index, isEOF = false)
            val response = ShuffleExchangeResponse(
              sourceIp = request.destinationIp,
              sourcePort = request.destinationPort,
              destinationIp = request.sourceIp,
              destinationPort = request.sourcePort,
              data = Some(dataChunk))

            responseObserver.onNext(response)
          }
      val emptyResponse = ShuffleExchangeResponse(
        sourceIp = request.destinationIp,
        sourcePort = request.destinationPort,
        destinationIp = request.sourceIp,
        destinationPort = request.sourcePort,
        data = Some(emptyDataChunk(size)))
      responseObserver.onNext(emptyResponse)
      logger.info(s"[Worker] Send single partition to ${request.sourceIp}:${request.sourcePort}")
      size = size + 1
    }

    // Send EOF
    val emptyResponse = ShuffleExchangeResponse(
      sourceIp = request.destinationIp,
      sourcePort = request.destinationPort,
      destinationIp = request.sourceIp,
      destinationPort = request.sourcePort,
      data = Some(emptyDataChunk(size)))
    responseObserver.onNext(emptyResponse)
    responseObserver.onCompleted()
    logger.info(s"[Worker] Finished sending data to ${request.sourceIp}:${request.sourcePort}")
  
    sendCompleteCount += 1
  }

  override def mergeData(request: MergeRequest): Future[MergeResponse] = {
    
    assert(workerFSM.getState() == WorkerPendingMergeRequest)

    workerFSM.transition(WorkerEventReceiveMergeRequest)
    assert(workerFSM.getState() == WorkerReceivedMergeRequest)
    logger.info("[Worker] Merge request received")

    val promise = Promise[MergeResponse]()

    workerFSM.transition(WorkerEventMergeData)
    assert(workerFSM.getState() == WorkerMergingData)

    Future {
      val tempFilePaths =
        getFileNames(outputDir)
          .filter(s => (s.take(Prefix.shuffling.length)) == Prefix.shuffling)
          .map(outputDir ++ "/" ++ _)
      val tournamentTree = new TournamentTree(tempFilePaths, outputDir ++ "/" ++ Prefix.merged)
      logger.info("[Worker] Merge Start")
      tournamentTree.merge()
    }.onComplete({
      case Success(value) => {
        workerFSM.transition(WorkerEventCompleteMerging)
        assert(workerFSM.getState() == WorkerMergingComplete)        
        logger.info("[Worker] Merge Complete")
        val response = MergeResponse(isMergeSuccessful = true)

        workerFSM.transition(WorkerEventSendMergeResponse)
        assert(workerFSM.getState() == WorkerSendingMergeResponse)
        logger.info("[Worker] Sending merge response")
        promise.success(response)
      }
      case Failure(exception) => {
        logger.error("[Worker] Merge Failed with")
        logger.error(exception)
        val response = MergeResponse(isMergeSuccessful = false)
        promise.failure(exception)
      }
    })

    workerFSM.transition(WorkerEventSendMergeResponseComplete)
    assert(workerFSM.getState() == WorkerSentMergeResponse)
    logger.info("[Worker] Sent merge response")

    promise.future
  }

  override def verifyKeyRange(request: VerificationRequest): Future[VerificationResponse] = {
    val response = VerificationResponse(isVerificationSuccessful = true)
    Future.successful(response)
  }
}
