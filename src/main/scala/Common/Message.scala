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
  var state: WorkerState = WORKER_INITIAL
}

class Message(messageType: MessageType, what_msg: String, destIp: String, destPort: Int) {
  val messasgeType: MessageType = messageType
  val msg: String = what_msg
  val ip: String = destIp
  val port: Int = destPort
}
