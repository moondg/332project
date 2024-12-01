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
case object WorkerConnectionEstablished extends WorkerState

// States of Sampling
case object WorkerPendingSampleRequest extends WorkerState
case object WorkerReceivedSampleRequest extends WorkerState
case object WorkerSampling extends WorkerState
case object WorkerSendingSampleResponse extends WorkerState
case object WorkerSendingSampleResponseFailure extends WorkerState
case object WorkerSentSampleResponse extends WorkerState

// States of Partitioning
case object WorkerPendingPartitionRequest extends WorkerState
case object WorkerReceivedPartitionRequest extends WorkerState
case object WorkerPartitioningData extends WorkerState
case object WorkerSendingPartitionResponse extends WorkerState
case object WorkerSendingPartitionResponseFailure extends WorkerState
case object WorkerSentPartitionResponse extends WorkerState

// States of Shuffling
case object WorkerPendingShuffleRequest extends WorkerState
case object WorkerReceivedShuffleRequest extends WorkerState
case object WorkerWaitingAllDataReceived extends WorkerState
case object WorkerReceivedAllData extends WorkerState
case object WorkerSendingShuffleResponse extends WorkerState
case object WorkerSendingShuffleResponseFailure extends WorkerState
case object WorkerSentShuffleResponse extends WorkerState

// States of Merging
case object WorkerPendingMergeRequest extends WorkerState
case object WorkerReceivedMergeRequest extends WorkerState
case object WorkerMergingData extends WorkerState
case object WorkerMergingComplete extends WorkerState
case object WorkerSendingMergeResponse extends WorkerState
case object WorkerSendingMergeResponseFailure extends WorkerState
case object WorkerSentMergeResponse extends WorkerState

// States of Verification
case object WorkerPendingVerificationRequest extends WorkerState
case object WorkerReceivedVerificationRequest extends WorkerState
case object WorkerVerifyingData extends WorkerState
case object WorkerVerificationComplete extends WorkerState
case object WorkerSendingVerificationResponse extends WorkerState
case object WorkerSendingVerificationResponseFailure extends WorkerState
case object WorkerSentVerificationResponse extends WorkerState

// States of Common
case object WorkerError extends WorkerState
case object WorkerFinished extends WorkerState

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

// Events of Worker
sealed trait WorkerEvent

// Events of Connection Establishment
case object WorkerEventConnectionEstablished extends WorkerEvent
case object WorkerEventReceiveSampleRequest extends WorkerEvent

// Events of Sampling
case object WorkerEventProceedSampling extends WorkerEvent
case object WorkerEventSendSampleResponse extends WorkerEvent
case object WorkerEventSendSampleResponseFailure extends WorkerEvent
case object WorkerEventSendSampleResponseComplete extends WorkerEvent

// Events of Partitioning
case object WorkerEventProceedPartitioning extends WorkerEvent
case object WorkerEventReceivePartitionRequest extends WorkerEvent
case object WorkerEventPartitionData extends WorkerEvent
case object WorkerEventSendPartitionResponse extends WorkerEvent
case object WorkerEventSendPartitionResponseFailure extends WorkerEvent
case object WorkerEventSendPartitionResponseComplete extends WorkerEvent

// Events of Shuffling
case object WorkerEventProceedShuffling extends WorkerEvent
case object WorkerEventReceiveShuffleRequest extends WorkerEvent
case object WorkerEventEstablishedInterworkerConnection extends WorkerEvent
case object WorkerEventReceivedAllData extends WorkerEvent
case object WorkerEventSendShuffleResponse extends WorkerEvent
case object WorkerEventSendShuffleResponseFailure extends WorkerEvent
case object WorkerEventSendShuffleResponseComplete extends WorkerEvent

// Events of Merging
case object WorkerEventProceedMerging extends WorkerEvent
case object WorkerEventReceiveMergeRequest extends WorkerEvent
case object WorkerEventMergeData extends WorkerEvent
case object WorkerEventCompleteMerging extends WorkerEvent
case object WorkerEventSendMergeResponse extends WorkerEvent
case object WorkerEventSendMergeResponseFailure extends WorkerEvent
case object WorkerEventSendMergeResponseComplete extends WorkerEvent

// Events of Verification
case object WorkerEventProceedVerification extends WorkerEvent
case object WorkerEventReceiveVerificationRequest extends WorkerEvent
case object WorkerEventVerifyData extends WorkerEvent
case object WorkerEventCompleteVerification extends WorkerEvent
case object WorkerEventVerificationFailure extends WorkerEvent
case object WorkerEventSendVerificationResponse extends WorkerEvent
case object WorkerEventSendVerificationResponseFailure extends WorkerEvent
case object WorkerEventSendVerificationResponseComplete extends WorkerEvent

// Events of Common
case object WorkerEventFinishSorting extends WorkerEvent

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

case class WorkerFSM(state: WorkerState, isVerificationNeeded: Boolean = false) {
  def getState(): WorkerState = state

  def transition(event: WorkerEvent): WorkerFSM = {
    state match {
      // Connection Establishment
      case WorkerInitial =>
        event match {
          case WorkerEventConnectionEstablished =>
            WorkerFSM(WorkerConnectionEstablished, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerConnectionEstablished =>
        event match {
          case WorkerEventProceedSampling =>
            WorkerFSM(WorkerPendingSampleRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
        
      // Sampling
      case WorkerPendingSampleRequest =>
        event match {
          case WorkerEventReceiveSampleRequest =>
            WorkerFSM(WorkerReceivedSampleRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedSampleRequest =>
        event match {
          case WorkerEventSendSampleResponse =>
            WorkerFSM(WorkerSendingSampleResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingSampleResponse =>
        event match {
          case WorkerEventSendSampleResponseComplete =>
            WorkerFSM(WorkerSentSampleResponse, isVerificationNeeded)
          case WorkerEventSendSampleResponseFailure =>
            WorkerFSM(WorkerSendingSampleResponseFailure, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingSampleResponseFailure =>
        event match {
          case WorkerEventSendSampleResponse =>
            WorkerFSM(WorkerSendingSampleResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSentSampleResponse =>
        event match {
          case WorkerEventProceedPartitioning =>
            WorkerFSM(WorkerPendingPartitionRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }

      // Partitioning
      case WorkerPendingPartitionRequest =>
        event match {
          case WorkerEventReceivePartitionRequest =>
            WorkerFSM(WorkerReceivedPartitionRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedPartitionRequest =>
        event match {
          case WorkerEventPartitionData =>
            WorkerFSM(WorkerPartitioningData, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerPartitioningData =>
        event match {
          case WorkerEventSendPartitionResponse =>
            WorkerFSM(WorkerSendingPartitionResponse, isVerificationNeeded) 
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingPartitionResponse =>
        event match {
          case WorkerEventSendPartitionResponseComplete =>
            WorkerFSM(WorkerSentPartitionResponse, isVerificationNeeded)
          case WorkerEventSendPartitionResponseFailure =>
            WorkerFSM(WorkerSendingPartitionResponseFailure, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        } 
      case WorkerSendingPartitionResponseFailure =>
        event match {
          case WorkerEventSendPartitionResponse =>
            WorkerFSM(WorkerSendingPartitionResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSentPartitionResponse =>
        event match {
          case WorkerEventProceedShuffling =>
            WorkerFSM(WorkerPendingShuffleRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }

      // Shuffling
      case WorkerPendingShuffleRequest =>
        event match {
          case WorkerEventReceiveShuffleRequest =>
            WorkerFSM(WorkerReceivedShuffleRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedShuffleRequest =>
        event match {
          case WorkerEventEstablishedInterworkerConnection =>
            WorkerFSM(WorkerWaitingAllDataReceived, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerWaitingAllDataReceived =>
        event match {
          case WorkerEventReceivedAllData =>
            WorkerFSM(WorkerReceivedAllData, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedAllData =>
        event match {
          case WorkerEventSendShuffleResponse =>
            WorkerFSM(WorkerSendingShuffleResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingShuffleResponse =>
        event match {
          case WorkerEventSendShuffleResponseComplete =>
            WorkerFSM(WorkerSentShuffleResponse, isVerificationNeeded)
          case WorkerEventSendShuffleResponseFailure =>
            WorkerFSM(WorkerSendingShuffleResponseFailure, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingShuffleResponseFailure =>
        event match {
          case WorkerEventSendShuffleResponse =>
            WorkerFSM(WorkerSendingShuffleResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSentShuffleResponse =>
        event match {
          case WorkerEventProceedMerging =>
            WorkerFSM(WorkerPendingMergeRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      
      // Merging
      case WorkerPendingMergeRequest =>
        event match {
          case WorkerEventReceiveMergeRequest =>
            WorkerFSM(WorkerReceivedMergeRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedMergeRequest =>
        event match {
          case WorkerEventMergeData =>
            WorkerFSM(WorkerMergingData, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerMergingData =>
        event match {
          case WorkerEventCompleteMerging =>
            WorkerFSM(WorkerMergingComplete, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerMergingComplete =>
        event match {
          case WorkerEventSendMergeResponse =>
            WorkerFSM(WorkerSendingMergeResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingMergeResponse =>
        event match {
          case WorkerEventSendMergeResponseComplete =>
            WorkerFSM(WorkerSentMergeResponse, isVerificationNeeded)
          case WorkerEventSendMergeResponseFailure =>
            WorkerFSM(WorkerSendingMergeResponseFailure, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingMergeResponseFailure =>
        event match {
          case WorkerEventSendMergeResponse =>
            WorkerFSM(WorkerSendingMergeResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSentMergeResponse =>
        event match {
          case WorkerEventProceedVerification =>
            isVerificationNeeded match {
              case true => WorkerFSM(WorkerPendingVerificationRequest, isVerificationNeeded)
              case false => WorkerFSM(WorkerError, isVerificationNeeded)
            }
          case WorkerEventFinishSorting =>
            WorkerFSM(WorkerFinished, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      
      // Verification
      case WorkerPendingVerificationRequest =>
        event match {
          case WorkerEventReceiveVerificationRequest =>
            WorkerFSM(WorkerReceivedVerificationRequest, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerReceivedVerificationRequest =>
        event match {
          case WorkerEventVerifyData =>
            WorkerFSM(WorkerVerifyingData, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerVerifyingData =>
        event match {
          case WorkerEventCompleteVerification =>
            WorkerFSM(WorkerVerificationComplete, isVerificationNeeded)
          case WorkerEventVerificationFailure =>
            WorkerFSM(WorkerError, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerVerificationComplete =>
        event match {
          case WorkerEventSendVerificationResponse =>
            WorkerFSM(WorkerSendingVerificationResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingVerificationResponse =>
        event match {
          case WorkerEventSendVerificationResponseComplete =>
            WorkerFSM(WorkerSentVerificationResponse, isVerificationNeeded)
          case WorkerEventSendVerificationResponseFailure =>
            WorkerFSM(WorkerSendingVerificationResponseFailure, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSendingVerificationResponseFailure =>
        event match {
          case WorkerEventSendVerificationResponse =>
            WorkerFSM(WorkerSendingVerificationResponse, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      case WorkerSentVerificationResponse =>
        event match {
          case WorkerEventFinishSorting =>
            WorkerFSM(WorkerFinished, isVerificationNeeded)
          case _ => WorkerFSM(WorkerError, isVerificationNeeded)
        }
      
      case WorkerFinished => WorkerFSM(WorkerFinished, isVerificationNeeded)
      
      case _ => WorkerFSM(WorkerError, isVerificationNeeded)
    }
  }
}