package Common

sealed trait MessageType
case object SyncronizationRequest extends MessageType
case object SyncronizationResponse extends MessageType
case object ParseRequest extends MessageType
case object ParseResponse extends MessageType
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

class WorkerStatus(ip_args: String, port_args: Int) {
  val ip: String = ip_args
  val port: Int = port_args
  var keyRange: (String, String) = null
  var state: WorkerState = WORKER_INITIAL
}

class Message(which_type_of_msg: MessageType, what_msg: String, to_whom_ip: String, to_whom_port: Int) {
  val msgType: MessageType = which_type_of_msg
  val msg: String = what_msg
  val ip: String = to_whom_ip
  val port: Int = to_whom_port
}