package Common

sealed trait MessageType

case object EstablishmentRequest extends MessageType
case object EstablishmentResponse extends MessageType
case object SamplingRequest extends MessageType
case object SamplingResponse extends MessageType
case object PartitioningRequest extends MessageType
case object PartitioningResponse extends MessageType
case object InternalSortRequest extends MessageType
case object InternalSortResponse extends MessageType
case object ShuffleRequest extends MessageType
case object ShuffleResponse extends MessageType
case object MergeRequest extends MessageType
case object MergeResponse extends MessageType

class WorkerStatus(_ip: String, _port: Int) {
  val ip: String = _ip
  val port: Int = _port
  var keyRange: (String, String) = null
  var state: WorkerState = WorkerInitial
}

class Message(
    msgType: MessageType,
    msg: String,
    destIp: String,
    destPort: Int,
    srcIp: String,
    srcPort: Int) {
  val messasgeType: MessageType = msgType
  val message: String = msg
  val destinationIp: String = destIp
  val destinationPort: Int = destPort
  val sourceIp: String = srcIp
  val sourcePort: Int = srcPort
}
