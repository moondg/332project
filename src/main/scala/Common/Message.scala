package Common

import Network.Network.{IPAddr, Port}

sealed trait MessageType

case object EstablishmentRequest extends MessageType
case object EstablishmentResponse extends MessageType
case object SamplingRequest extends MessageType
case object SamplingResponse extends MessageType
case object PartitioningRequest extends MessageType
case object PartitioningResponse extends MessageType
case object InternalSortRequest extends MessageType
case object InternalSortResponse extends MessageType
case object ShuffleRunRequest extends MessageType
case object ShuffleRunResponse extends MessageType
case object ShuffleExchangeRequest extends MessageType
case object ShuffleExchangeResponse extends MessageType
case object MergeRequest extends MessageType
case object MergeResponse extends MessageType

class WorkerStatus(val ip: IPAddr, val port: Port) {
  var keyRange: (String, String) = null
  var state: WorkerState = WorkerInitial
}

class Message(
    val msgType: MessageType,
    val msg: String,
    val destIp: String,
    val destPort: Int,
    val srcIp: String,
    val srcPort: Int) {}
