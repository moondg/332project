package Network

import Network._

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

class NetworkClient(
    val master: Node,
    val client: Node,
    val inputDirs: List[String],
    val outputDir: String,
    val executionContext: ExecutionContext)
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
    clientService = new ClientImpl(inputDirs, outputDir, client)
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
        logger.info("[Worker] Connection established")
      } else {
        logger.info("[Worker] Connection failed")
      }

    } catch {
      case e: Exception => logger.error(e)
    }
  }

  def shutdown(): Unit = {}

  def sendSamples(sample: List[Key], node: Node): Unit = {}
  def sendRecords(records: List[Record], node: Node): Unit = {}

  def send_unmatched_data(): Unit = {}

  def wait_until_all_data_received(): Unit = {}
}

class ClientImpl(val inputDirs: List[String], val outputDir: String, val thisClient: Node)
    extends WorkerServiceGrpc.WorkerService
    with Logging {

  val fileNames = inputDirs.map(getFileNames).flatten
  val filePaths = getAllFilePaths(inputDirs)

  var keyRangeTable: Table = null

  override def sampleData(
      request: SampleRequest,
      responseObserver: StreamObserver[SampleResponse]): Unit = {

    val percentageOfSampling = request.percentageOfSampling

    var index = 0

    logger.info("[Worker] Start sending samples")
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

    logger.info("[Worker] End sending samples")
  }

  override def partitionData(request: PartitionRequest): Future[PartitionResponse] = {
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

    val promise = Promise[PartitionResponse]()
    logger.info("[Worker] Partition Start")

    val processingFutures = (filePaths zip fileNames).map { case (filePath, fileName) =>
      Future {
        val block = blockFromFile(filePath)
        logger.info(s"[Worker] ${fileName} start")

        try {
          for {
            (partition, node) <- dividePartition(block.block.sorted, keyRangeTable)
            outFilePath = s"${outputDir}/${node._1}:${node._2}_${stringHash(filePath)}_${fileName}"
          } yield {
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
          promise.success(PartitionResponse(isPartitioningSuccessful = true))
        } else {
          val firstFailure = results.collectFirst { case Failure(e) => e }.get
          promise.failure(firstFailure)
        }
      case Failure(exception) =>
        promise.failure(exception)
    }

    promise.future
  }

  override def runShuffle(request: ShuffleRunRequest): Future[ShuffleRunResponse] = {

    // Get clients
    val clients =
      keyRangeTable.filter(tableRow => tableRow._2 != thisClient).map { tableRow => tableRow._2 }
    val channels = clients.map { case (ip, port) =>
      ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build()
    }
    val stubs = channels.map { channel => WorkerServiceGrpc.stub(channel) }

    // Send ExchangeRequest to all clients
    val exchangeResponses: Seq[Future[Boolean]] = clients.zip(stubs).map { case (client, stub) =>
      val promise = Promise[Boolean]()
      val exchangeRequest = ShuffleExchangeRequest(
        sourceIp = thisClient._1,
        sourcePort = thisClient._2,
        destinationIp = client._1,
        destinationPort = client._2)

      var haveReachedEOF = false

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
                val outFilePath = s"${outputDir}/received_${client._1}_${dataChunk.chunkIndex}"
                writeFile(outFilePath, List(record))
              }
            }
            case None => onError(new Exception("Received empty data chunk"))
          }
        }

        override def onError(t: Throwable): Unit = {
          promise.failure(t)
        }

        override def onCompleted(): Unit = {
          if (haveReachedEOF) {
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
        ShuffleRunResponse(isShufflingSuccessful = isExchangeSuccessful)
      } catch {
        case e: Exception =>
          logger.error(e)
          ShuffleRunResponse(isShufflingSuccessful = false)
      }

    // Close channels
    channels.foreach { channel => channel.shutdown() }

    logger.info(s"[Worker] Finished Receiving data from other clients")

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
    // TODO: Pack values
    var size = 0
    val exchangeFileNames = getFileNames(outputDir)
      .filter(s => (s takeWhile (_ != '_')) == s"${request.sourceIp}:${request.sourcePort}")
      .map(s => s.dropWhile(_ != '_'))

    exchangeFileNames.foreach { exchangeFileName =>
      val data: Seq[Record] =
        readFile(outputDir ++ "/" ++ exchangeFileName)
          .grouped(Constant.Size.record)
          .map(recordFrom)
          .toSeq
      size = size + data.length
      logger.info(s"[Worker] Sending data to ${request.sourceIp}:${request.sourcePort}")
      data.zipWithIndex.foreach { case (record, index) =>
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
  }

  override def mergeData(request: MergeRequest): Future[MergeResponse] = {
    val promise = Promise[MergeResponse]()

    Future {
      val tempFilePaths = getAllFilePaths(List(outputDir))
      val tournamentTree = new TournamentTree(tempFilePaths, outputDir)
      logger.info("[Worker] Merge Start")
      tournamentTree.merge()
    }.onComplete({
      case Success(value) => {
        logger.info("[Worker] Merge Complete")
        val response = MergeResponse(isMergeSuccessful = true)
        promise.success(response)
      }
      case Failure(exception) => {
        logger.error("[Worker] Merge Failed with")
        logger.error(exception)
        val response = MergeResponse(isMergeSuccessful = false)
        promise.failure(exception)
      }
    })

    promise.future
  }

  override def verifyKeyRange(request: VerificationRequest): Future[VerificationResponse] = {
    val response = VerificationResponse(isVerificationSuccessful = true)
    Future.successful(response)
  }
}
