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