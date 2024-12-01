import org.scalacheck._
import Prop.forAll
import Common._

object FSMTestSpecification extends Properties("FSMTest") {

  // Test property to check if FiniteStateMachine is working correctly
  property("Does Connection Establishment of Master works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterInitial)
    masterFSM = masterFSM.transition(MasterEventConnectionEstablished)
    masterFSM.getState() == MasterConnectionEstablished
  }

  property("Does Sampling of Master works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterConnectionEstablished)
    
    masterFSM = masterFSM.transition(MasterEventProceedSampling)
    val correctState1 = masterFSM.getState() == MasterSendingSampleRequest

    masterFSM = masterFSM.transition(MasterEventSendSampleRequest)
    val correctState2 = masterFSM.getState() == MasterPendingSampleResponse
    
    masterFSM = masterFSM.transition(MasterEventReceiveSampleResponseFailure)
    val correctState3 = masterFSM.getState() == MasterReceivedSampleResponseFailure
    
    masterFSM = masterFSM.transition(MasterEventSendSampleRequest)
    val correctState4 = masterFSM.getState() == MasterPendingSampleResponse
    
    masterFSM = masterFSM.transition(MasterEventReceiveSampleResponse)
    val correctState5 = masterFSM.getState() == MasterReceivedSampleResponse
    
    correctState1 && correctState2 && correctState3 && correctState4 && correctState5
  }

  property("Does Partitioning of Master works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterReceivedSampleResponse)
    
    masterFSM = masterFSM.transition(MasterEventMakePartition)
    val correctState1 = masterFSM.getState() == MasterMakingPartition
    
    masterFSM = masterFSM.transition(MasterEventMadePartition)
    val correctState2 = masterFSM.getState() == MasterSendingPartitionRequest
    
    masterFSM = masterFSM.transition(MasterEventSendPartitionRequest)
    val correctState3 = masterFSM.getState() == MasterPendingPartitionResponse
    
    masterFSM = masterFSM.transition(MasterEventReceivePartitionResponseFailure)
    val correctState4 = masterFSM.getState() == MasterReceivedPartitionResponseFailure

    masterFSM = masterFSM.transition(MasterEventSendPartitionRequest)
    val correctState5 = masterFSM.getState() == MasterPendingPartitionResponse

    masterFSM = masterFSM.transition(MasterEventReceivePartitionResponse)
    val correctState6 = masterFSM.getState() == MasterReceivedPartitionResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6
  }

  property("Does Shuffling of Master works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterReceivedPartitionResponse)
    
    masterFSM = masterFSM.transition(MasterEventProceedShuffling)
    val correctState1 = masterFSM.getState() == MasterSendingShuffleRequest

    masterFSM = masterFSM.transition(MasterEventSendShuffleRequest)
    val correctState2 = masterFSM.getState() == MasterPendingShuffleResponse
    
    masterFSM = masterFSM.transition(MasterEventReceiveShuffleResponseFailure)
    val correctState3 = masterFSM.getState() == MasterReceivedShuffleResponseFailure

    masterFSM = masterFSM.transition(MasterEventSendShuffleRequest)
    val correctState4 = masterFSM.getState() == MasterPendingShuffleResponse

    masterFSM = masterFSM.transition(MasterEventReceiveShuffleResponse)
    val correctState5 = masterFSM.getState() == MasterReceivedShuffleResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5
  }

  property("Does Merging of Master works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterReceivedShuffleResponse)
    
    masterFSM = masterFSM.transition(MasterEventProceedMerging)
    val correctState1 = masterFSM.getState() == MasterSendingMergeRequest
    
    masterFSM = masterFSM.transition(MasterEventSendMergeRequest)
    val correctState2 = masterFSM.getState() == MasterPendingMergeResponse
    
    masterFSM = masterFSM.transition(MasterEventReceiveMergeResponseFailure)
    val correctState3 = masterFSM.getState() == MasterReceivedMergeResponseFailure

    masterFSM = masterFSM.transition(MasterEventSendMergeRequest)
    val correctState4 = masterFSM.getState() == MasterPendingMergeResponse

    masterFSM = masterFSM.transition(MasterEventReceiveMergeResponse)
    val correctState5 = masterFSM.getState() == MasterReceivedMergeResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5
  }

  property("Does Master Finished works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterReceivedMergeResponse)
    
    masterFSM = masterFSM.transition(MasterEventFinishSorting)
    val correctState1 = masterFSM.getState() == MasterFinished

    correctState1
  }

  property("Does Verification of sorting works correctly") = forAll { _: Unit =>
    var masterFSM = new MasterFSM(MasterReceivedMergeResponse, true)

    masterFSM = masterFSM.transition(MasterEventProceedVerification)
    val correctState1 = masterFSM.getState() == MasterSendingVerificationRequest

    masterFSM = masterFSM.transition(MasterEventSendVerificationRequest)
    val correctState2 = masterFSM.getState() == MasterPendingVerificationResponse

    masterFSM = masterFSM.transition(MasterEventReceiveVerificationResponseFailure)
    val correctState3 = masterFSM.getState() == MasterReceivedVerificationResponseFailure

    masterFSM = masterFSM.transition(MasterEventSendVerificationRequest)
    val correctState4 = masterFSM.getState() == MasterPendingVerificationResponse

    masterFSM = masterFSM.transition(MasterEventReceiveVerificationResponse)
    val correctState5 = masterFSM.getState() == MasterVerifyingInterWorker

    var masterFSM1 = masterFSM.transition(MasterEventFinishSorting)
    val correctState6 = masterFSM1.getState() == MasterFinished

    var masterFSM2 = masterFSM.transition(MasterEventVerificationInterWorkerFailure)
    val correctState7 = masterFSM2.getState() == MasterError

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6 && correctState7
  }

  property("Does Connection Establishment of Worker works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerInitial)
    workerFSM = workerFSM.transition(WorkerEventConnectionEstablished)
    workerFSM.getState() == WorkerConnectionEstablished
  }

  property("Does Sampling of Worker works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerConnectionEstablished)
    
    workerFSM = workerFSM.transition(WorkerEventProceedSampling)
    val correctState1 = workerFSM.getState() == WorkerPendingSampleRequest

    workerFSM = workerFSM.transition(WorkerEventReceiveSampleRequest)
    val correctState2 = workerFSM.getState() == WorkerReceivedSampleRequest
    
    workerFSM = workerFSM.transition(WorkerEventSendSampleResponse)
    val correctState3 = workerFSM.getState() == WorkerSendingSampleResponse
    
    workerFSM = workerFSM.transition(WorkerEventSendSampleResponseFailure)
    val correctState4 = workerFSM.getState() == WorkerSendingSampleResponseFailure

    workerFSM = workerFSM.transition(WorkerEventSendSampleResponse)
    val correctState5 = workerFSM.getState() == WorkerSendingSampleResponse

    workerFSM = workerFSM.transition(WorkerEventSendSampleResponseComplete)
    val correctState6 = workerFSM.getState() == WorkerSentSampleResponse
    
    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6
  }

  property("Does Partitioning of Worker works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerSentSampleResponse)
    
    workerFSM = workerFSM.transition(WorkerEventProceedPartitioning)
    val correctState1 = workerFSM.getState() == WorkerPendingPartitionRequest

    workerFSM = workerFSM.transition(WorkerEventReceivePartitionRequest)
    val correctState2 = workerFSM.getState() == WorkerReceivedPartitionRequest

    workerFSM = workerFSM.transition(WorkerEventPartitionData)
    val correctState3 = workerFSM.getState() == WorkerPartitioningData

    workerFSM = workerFSM.transition(WorkerEventSendPartitionResponse)
    val correctState4 = workerFSM.getState() == WorkerSendingPartitionResponse

    workerFSM = workerFSM.transition(WorkerEventSendPartitionResponseFailure)
    val correctState5 = workerFSM.getState() == WorkerSendingPartitionResponseFailure

    workerFSM = workerFSM.transition(WorkerEventSendPartitionResponse)
    val correctState6 = workerFSM.getState() == WorkerSendingPartitionResponse

    workerFSM = workerFSM.transition(WorkerEventSendPartitionResponseComplete)
    val correctState7 = workerFSM.getState() == WorkerSentPartitionResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6 && correctState7
  }

  property("Does Shuffling of Worker works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerSentPartitionResponse)
    
    workerFSM = workerFSM.transition(WorkerEventProceedShuffling)
    val correctState1 = workerFSM.getState() == WorkerPendingShuffleRequest

    workerFSM = workerFSM.transition(WorkerEventReceiveShuffleRequest)
    val correctState2 = workerFSM.getState() == WorkerReceivedShuffleRequest

    workerFSM = workerFSM.transition(WorkerEventEstablishedInterworkerConnection)
    val correctState3 = workerFSM.getState() == WorkerWaitingAllDataReceived

    workerFSM = workerFSM.transition(WorkerEventReceivedAllData)
    val correctState4 = workerFSM.getState() == WorkerReceivedAllData

    workerFSM = workerFSM.transition(WorkerEventSendShuffleResponse)
    val correctState5 = workerFSM.getState() == WorkerSendingShuffleResponse

    workerFSM = workerFSM.transition(WorkerEventSendShuffleResponseFailure)
    val correctState6 = workerFSM.getState() == WorkerSendingShuffleResponseFailure

    workerFSM = workerFSM.transition(WorkerEventSendShuffleResponse)
    val correctState7 = workerFSM.getState() == WorkerSendingShuffleResponse

    workerFSM = workerFSM.transition(WorkerEventSendShuffleResponseComplete)
    val correctState8 = workerFSM.getState() == WorkerSentShuffleResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6 && correctState7 && correctState8
  }

  property("Does Merging of Worker works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerSentShuffleResponse)
    
    workerFSM = workerFSM.transition(WorkerEventProceedMerging)
    val correctState1 = workerFSM.getState() == WorkerPendingMergeRequest

    workerFSM = workerFSM.transition(WorkerEventReceiveMergeRequest)
    val correctState2 = workerFSM.getState() == WorkerReceivedMergeRequest

    workerFSM = workerFSM.transition(WorkerEventMergeData)
    val correctState3 = workerFSM.getState() == WorkerMergingData

    workerFSM = workerFSM.transition(WorkerEventCompleteMerging)
    val correctState4 = workerFSM.getState() == WorkerMergingComplete

    workerFSM = workerFSM.transition(WorkerEventSendMergeResponse)
    val correctState5 = workerFSM.getState() == WorkerSendingMergeResponse

    workerFSM = workerFSM.transition(WorkerEventSendMergeResponseFailure)
    val correctState6 = workerFSM.getState() == WorkerSendingMergeResponseFailure

    workerFSM = workerFSM.transition(WorkerEventSendMergeResponse)
    val correctState7 = workerFSM.getState() == WorkerSendingMergeResponse

    workerFSM = workerFSM.transition(WorkerEventSendMergeResponseComplete)
    val correctState8 = workerFSM.getState() == WorkerSentMergeResponse

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6 && correctState7 && correctState8
  }

  property("Does Worker Finished works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerSentMergeResponse)
    
    workerFSM = workerFSM.transition(WorkerEventFinishSorting)
    val correctState1 = workerFSM.getState() == WorkerFinished

    correctState1
  }

  property("Does Verification of sorting works correctly") = forAll { _: Unit =>
    var workerFSM = new WorkerFSM(WorkerSentMergeResponse, true)

    workerFSM = workerFSM.transition(WorkerEventProceedVerification)
    val correctState1 = workerFSM.getState() == WorkerPendingVerificationRequest

    workerFSM = workerFSM.transition(WorkerEventReceiveVerificationRequest)
    val correctState2 = workerFSM.getState() == WorkerReceivedVerificationRequest

    workerFSM = workerFSM.transition(WorkerEventVerifyData)
    val correctState3 = workerFSM.getState() == WorkerVerifyingData

    var workerFSM1 = workerFSM.transition(WorkerEventVerificationFailure)
    val correctState4 = workerFSM1.getState() == WorkerError

    workerFSM = workerFSM.transition(WorkerEventCompleteVerification)
    val correctState5 = workerFSM.getState() == WorkerVerificationComplete

    workerFSM = workerFSM.transition(WorkerEventSendVerificationResponse)
    val correctState6 = workerFSM.getState() == WorkerSendingVerificationResponse

    workerFSM = workerFSM.transition(WorkerEventSendVerificationResponseFailure)
    val correctState7 = workerFSM.getState() == WorkerSendingVerificationResponseFailure

    workerFSM = workerFSM.transition(WorkerEventSendVerificationResponse)
    val correctState8 = workerFSM.getState() == WorkerSendingVerificationResponse

    workerFSM = workerFSM.transition(WorkerEventSendVerificationResponseComplete)
    val correctState9 = workerFSM.getState() == WorkerSentVerificationResponse

    workerFSM = workerFSM.transition(WorkerEventFinishSorting)
    val correctState10 = workerFSM.getState() == WorkerFinished

    correctState1 && correctState2 && correctState3 && correctState4 && correctState5 && correctState6 && correctState7 && correctState8 && correctState9 && correctState10
  }
}
