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
}
