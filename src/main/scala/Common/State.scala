package Common

// States of Master
sealed trait MasterState

// States of Connection Establishment
case object MasterInitial extends MasterState
case object MasterConnectionEstablished extends MasterState

// States of Sampling
case object MasterSendingSampleRequest extends MasterState
case object MasterPendingSampleResponse extends MasterState
case object MasterReceivedSampleResponse extends MasterState
case object MasterReceivedSampleResponseFailure extends MasterState

// States of Partitioning
case object MasterMakingPartition extends MasterState
case object MasterSendingPartitionRequest extends MasterState
case object MasterPendingPartitionResponse extends MasterState
case object MasterReceivedPartitionResponse extends MasterState
case object MasterReceivedPartitionResponseFailure extends MasterState

// State of Shuffling
case object MasterSendingShuffleRequest extends MasterState
case object MasterPendingShuffleResponse extends MasterState
case object MasterReceivedShuffleResponse extends MasterState
case object MasterReceivedShuffleResponseFailure extends MasterState

// State of Merging
case object MasterSendingMergeRequest extends MasterState
case object MasterPendingMergeResponse extends MasterState
case object MasterReceivedMergeResponse extends MasterState
case object MasterReceivedMergeResponseFailure extends MasterState

// State of Verification
case object MasterSendingVerificationRequest extends MasterState
case object MasterPendingVerificationResponse extends MasterState
case object MasterReceivedVerificationResponse extends MasterState
case object MasterReceivedVerificationResponseFailure extends MasterState
case object MasterVerifyingInterWorker extends MasterState

// State of Common
case object MasterFinished extends MasterState
case object MasterError extends MasterState // Dead State

// States of Worker
sealed trait WorkerState

// States of Connection Establishment
case object WorkerInitial extends WorkerState
case object WorkerEstablished extends WorkerState

// States of Sampling
case object WorkerPendingSampleRequest extends WorkerState
case object WorkerSampling extends WorkerState
case object WorkerSendingSample extends WorkerState
case object WorkerSendedSample extends WorkerState

// States of Partitioning
case object WorkerCheckPartition extends WorkerState
case object WorkerSendingUnmatchedData extends WorkerState
case object WorkerWaitingAllDataReceived extends WorkerState
case object WorkerDone extends WorkerState
case object WorkerError extends WorkerState

// Events of Master
sealed trait MasterEvent
// Events of Connection Establishment
case object MasterEventConnectionEstablished extends MasterEvent

// Events of Sampling
case object MasterEventProceedSampling extends MasterEvent
case object MasterEventSendSampleRequest extends MasterEvent
case object MasterEventReceiveSampleResponse extends MasterEvent
case object MasterEventReceiveSampleResponseFailure extends MasterEvent

// Events of Partitioning
case object MasterEventMakePartition extends MasterEvent
case object MasterEventMadePartition extends MasterEvent
case object MasterEventSendPartitionRequest extends MasterEvent
case object MasterEventReceivePartitionResponse extends MasterEvent
case object MasterEventReceivePartitionResponseFailure extends MasterEvent

// Events of Shuffling
case object MasterEventProceedShuffling extends MasterEvent
case object MasterEventSendShuffleRequest extends MasterEvent
case object MasterEventReceiveShuffleResponse extends MasterEvent
case object MasterEventReceiveShuffleResponseFailure extends MasterEvent

// Events of Merging
case object MasterEventProceedMerging extends MasterEvent
case object MasterEventSendMergeRequest extends MasterEvent
case object MasterEventReceiveMergeResponse extends MasterEvent
case object MasterEventReceiveMergeResponseFailure extends MasterEvent

// Events of Verification
case object MasterEventProceedVerification extends MasterEvent
case object MasterEventSendVerificationRequest extends MasterEvent
case object MasterEventReceiveVerificationResponse extends MasterEvent
case object MasterEventReceiveVerificationResponseFailure extends MasterEvent
case object MasterEventVerificationInterWorkerFailure extends MasterEvent


// Events of Common
case object MasterEventFinishSorting extends MasterEvent

// Finite State Machine for Master that tracks the state of the each Master-Worker connection
case class MasterFSM(state: MasterState, isVerificationNeeded: Boolean = false) {
  def getState(): MasterState = state

  def transition(event: MasterEvent): MasterFSM = {
    state match {
      // Connection Establishment
      case MasterInitial =>
        event match {
          case MasterEventConnectionEstablished =>
            MasterFSM(MasterConnectionEstablished, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      
      case MasterConnectionEstablished =>
        event match {
          case MasterEventProceedSampling =>
            MasterFSM(MasterSendingSampleRequest, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      // Sampling
      case MasterSendingSampleRequest =>
        event match {
          case MasterEventSendSampleRequest =>
            MasterFSM(MasterPendingSampleResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterPendingSampleResponse =>
        event match {
          case MasterEventReceiveSampleResponse =>
            MasterFSM(MasterReceivedSampleResponse, isVerificationNeeded)
          case MasterEventReceiveSampleResponseFailure =>
            MasterFSM(MasterReceivedSampleResponseFailure, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedSampleResponse=>
        event match {
          case MasterEventMakePartition =>
            MasterFSM(MasterMakingPartition, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedSampleResponseFailure =>
        event match {
          case MasterEventSendSampleRequest =>
            MasterFSM(MasterPendingSampleResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      // Partitioning
      case MasterMakingPartition =>
        event match {
          case MasterEventMadePartition => MasterFSM(MasterSendingPartitionRequest, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterSendingPartitionRequest =>
        event match {
          case MasterEventSendPartitionRequest =>
            MasterFSM(MasterPendingPartitionResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterPendingPartitionResponse =>
        event match {
          case MasterEventReceivePartitionResponse =>
            MasterFSM(MasterReceivedPartitionResponse, isVerificationNeeded)
          case MasterEventReceivePartitionResponseFailure =>
            MasterFSM(MasterReceivedPartitionResponseFailure, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedPartitionResponse =>
        event match {
          case MasterEventProceedShuffling =>
            MasterFSM(MasterSendingShuffleRequest, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedPartitionResponseFailure =>
        event match {
          case MasterEventSendPartitionRequest =>
            MasterFSM(MasterPendingPartitionResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      // Shuffling
      case MasterSendingShuffleRequest =>
        event match {
          case MasterEventSendShuffleRequest =>
            MasterFSM(MasterPendingShuffleResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterPendingShuffleResponse =>
        event match {
          case MasterEventReceiveShuffleResponse =>
            MasterFSM(MasterReceivedShuffleResponse, isVerificationNeeded)
          case MasterEventReceiveShuffleResponseFailure =>
            MasterFSM(MasterReceivedShuffleResponseFailure, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedShuffleResponse =>
        event match {
          case MasterEventProceedMerging =>
            MasterFSM(MasterSendingMergeRequest, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedShuffleResponseFailure =>
        event match {
          case MasterEventSendShuffleRequest =>
            MasterFSM(MasterPendingShuffleResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      // Merging
      case MasterSendingMergeRequest =>
        event match {
          case MasterEventSendMergeRequest =>
            MasterFSM(MasterPendingMergeResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterPendingMergeResponse =>
        event match {
          case MasterEventReceiveMergeResponse =>
            MasterFSM(MasterReceivedMergeResponse, isVerificationNeeded)
          case MasterEventReceiveMergeResponseFailure =>
            MasterFSM(MasterReceivedMergeResponseFailure, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedMergeResponse =>
        event match {
          case MasterEventProceedVerification =>
            isVerificationNeeded match {
              case true => MasterFSM(MasterSendingVerificationRequest, isVerificationNeeded)
              case false => MasterFSM(MasterError, isVerificationNeeded)
            }
          case MasterEventFinishSorting =>
            MasterFSM(MasterFinished, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterReceivedMergeResponseFailure =>
        event match {
          case MasterEventSendMergeRequest =>
            MasterFSM(MasterPendingMergeResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      // Verification
      case MasterSendingVerificationRequest =>
        event match {
          case MasterEventSendVerificationRequest =>
            MasterFSM(MasterPendingVerificationResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      case MasterPendingVerificationResponse =>
        event match {
          case MasterEventReceiveVerificationResponse =>
            MasterFSM(MasterVerifyingInterWorker, isVerificationNeeded)
          case MasterEventReceiveVerificationResponseFailure =>
            MasterFSM(MasterReceivedVerificationResponseFailure, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      case MasterReceivedVerificationResponse =>
        event match {
          case MasterEventFinishSorting =>
            MasterFSM(MasterVerifyingInterWorker, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }
      
      case MasterReceivedVerificationResponseFailure =>
        event match {
          case MasterEventSendVerificationRequest =>
            MasterFSM(MasterPendingVerificationResponse, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      case MasterVerifyingInterWorker =>
        event match {
          case MasterEventFinishSorting =>
            MasterFSM(MasterFinished, isVerificationNeeded)
          case MasterEventVerificationInterWorkerFailure =>
            MasterFSM(MasterError, isVerificationNeeded)
          case _ => MasterFSM(MasterError, isVerificationNeeded)
        }

      case MasterFinished => MasterFSM(MasterFinished, isVerificationNeeded)

      case _ => MasterFSM(MasterError, isVerificationNeeded)
    }
  }
}
