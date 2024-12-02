package Network

import Network._

// Import necessary scala libraries
import scala.concurrent.{ExecutionContext, Future, Promise, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.{Map, ListBuffer}
import scala.util.{Success, Failure}
import scala.annotation.tailrec

// Import necessary java libraries
import java.net.InetAddress
import java.util.concurrent.TimeUnit

// Import logging libraries
import org.apache.logging.log4j.scala.Logging

// Import necessary project libraries
import Common._
import Core._
import Core.Table._
import Core.Key._
import Core.Block._

// Import gRPC libraries
import io.grpc.{Server, ManagedChannelBuilder, ServerBuilder, Status}
import io.grpc.stub.StreamObserver

// Import protobuf messages and services
import message.establishment.{EstablishRequest, EstablishResponse}
import message.sampling.{SampleRequest, SampleResponse}
import message.partitioning.{PartitionRequest, PartitionResponse}
import message.shuffling.{ShuffleRunRequest, ShuffleRunResponse, ShuffleExchangeRequest, ShuffleExchangeResponse}
import message.merging.{MergeRequest, MergeResponse}
import message.verification.{VerificationRequest, VerificationResponse}
import message.service.{MasterServiceGrpc, WorkerServiceGrpc}
import message.common.{DataChunk, KeyRange, KeyRangeTableRow, KeyRangeTable}

// import Network.Network._

// Define the Network object
object Network {
  type IPAddr = String
  type Port = Int
  type Node = (IPAddr, Port)
}

class NetworkServer(port: Int, numberOfWorkers: Int, executionContext: ExecutionContext)
    extends Logging {

  var server: Server = null
  var state: MasterState = MasterInitial
  var clientList: ListBuffer[WorkerStatus] = ListBuffer.empty
  var sample: Array[String] = Array.empty[String]

  def startServer(): Unit = {
    server = ServerBuilder
      .forPort(port)
      .addService(MasterServiceGrpc.bindService(new ServerImpl(clientList), executionContext))
      .build()
      .start()
  }

  def ongoingServer(): Unit = {
    while (server != null && state != MasterFinished) {
      state match {
        case MasterReceivedSampleResponse => {
          pivot_check()
        }
        case MasterMakingPartition => {
          divideKeyRange()
        }
        case MasterPendingMergeResponse => {}
      }
    }
  }

  def stopServer(): Unit = {
    if (server != null) {
      server.shutdown.awaitTermination(1, TimeUnit.SECONDS)
      state = MasterFinished
    }
  }

  def sendMsg(msg: Message): Unit = {
    val msgType = msg.msgType
    // msgType match {}
  }

  def pivot_check(): Unit = {

    val f = Future {}

    f.onComplete {
      case Success(v) => {
        state = MasterReceivedSampleResponse
      }
      case Failure(e) => {
        state = MasterReceivedSampleResponseFailure
      }
    }

  }

  def ipLogging(): Unit = {
    @tailrec
    def clientIPLogging(clientList: List[WorkerStatus]): Unit = {
      assert(clientList != Nil)
      logger.info(s"Worker IP - ${clientList.head.ip}")
      if (clientList.tail != Nil) clientIPLogging(clientList.tail)
    }
    val ip = InetAddress.getLocalHost.getAddress
    logger.info(
      s"Master IP:Port - ${ip(0).toString}.${ip(1).toString}.${ip(2).toString}.${ip(3).toString}:${port.toString}")
    clientIPLogging(clientList.toList)
  }

  def divideKeyRange(): Unit = {
    val sampleCountPerWorker: Int = sample.length / numberOfWorkers
    @tailrec
    def divideKeyRangeRecur(
        data: List[Key],
        dataCount: Int,
        whoseRange: Int,
        rangeHead: Key): Unit = {
      if (whoseRange == numberOfWorkers - 1) {
        // (head, MAXIMUM)
        clientList(numberOfWorkers - 1).keyRange = (rangeHead, 0xff.toChar.toString * 10)
      } else if (dataCount == 1) {
        clientList(whoseRange).keyRange = (rangeHead, data.head)
        divideKeyRangeRecur(data.tail, sampleCountPerWorker, whoseRange + 1, data.head + 1)
      } else {
        divideKeyRangeRecur(data.tail, dataCount - 1, whoseRange, rangeHead)
      }
    }
    // rangeHead = MINIMUM
    divideKeyRangeRecur(sample.sorted.toList, sampleCountPerWorker, 0, 0x00.toChar.toString * 10)
  }
}

class ServerImpl(clientList: ListBuffer[WorkerStatus]) extends MasterServiceGrpc.MasterService {

  override def establishConnection(request: EstablishRequest): Future[EstablishResponse] = {

    val workerStatus = new WorkerStatus(request.workerIp, request.workerPort)

    clientList.synchronized {
      clientList += workerStatus
    }

    val response = EstablishResponse(isEstablishmentSuccessful = true)
    Future.successful(response)
  }
}