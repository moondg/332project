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

// Import gRPC libraries
import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
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

  def start(): Unit = {
    clientService = new ClientImpl(inputDirs, outputDir)
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

class ClientImpl(val inputDirs: List[String], val outputDir: String)
    extends WorkerServiceGrpc.WorkerService
    with Logging {

  val fileNames = inputDirs.map(getFileNames).flatten
  val filePaths = getAllFilePaths(inputDirs)

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
        if (index % 1000 == 0) logger.info(s"[Worker] Send ${index} records")
      }
    }

    val emptyResponse = SampleResponse(
      isSamplingSuccessful = true,
      sample = Some(DataChunk(data = ByteString.EMPTY, chunkIndex = index, isEOF = true)))
    responseObserver.onNext(emptyResponse)
    responseObserver.onCompleted()

    logger.info("[Worker] End sending samples")
  }

  override def partitionData(request: PartitionRequest): Future[PartitionResponse] = {
    logger.info("[Worker] Partitioning request received")
    val keyRangeTable: Table = request.table match {
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
        val block = makeBlockFromFile(filePath)
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

    val response = ShuffleRunResponse(isShufflingSuccessful = true)
    Future.successful(response)
  }

  override def exchangeData(request: ShuffleExchangeRequest): Future[ShuffleExchangeResponse] = {

    // Pack values and send data
    val data = Seq(???)

    val response = ShuffleExchangeResponse(
      sourceIp = "",
      sourcePort = 0,
      destinationIp = "",
      destinationPort = 0,
      data = data)
    Future.successful(response)
  }

  override def mergeData(request: MergeRequest): Future[MergeResponse] = {
    val response = MergeResponse(isMergeSuccessful = true)
    Future.successful(response)
  }

  override def verifyKeyRange(request: VerificationRequest): Future[VerificationResponse] = {
    val response = VerificationResponse(isVerificationSuccessful = true)
    Future.successful(response)
  }
}
