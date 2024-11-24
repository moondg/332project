package Common

sealed trait MasterState
case object MASTER_INITIAL extends MasterState
case object MASTER_GET_SAMPLE extends MasterState
case object MASTER_FAIL_TO_GET_SAMPLE extends MasterState
case object MASTER_MAKE_PARTITION extends MasterState
case object MASTER_WAIT_SORT extends MasterState
case object MASTER_DONE extends MasterState
case object MASTER_FINISH extends MasterState

sealed trait WorkerState
case object WORKER_INITIAL extends WorkerState
case object WORKER_SEND_SAMPLE extends WorkerState
case object WORKER_CHECK_PARTITION extends WorkerState
case object WORKER_SENDING_UNMATCHED_DATA extends WorkerState
case object WORKER_WAITING_ALL_DATA_RECEIVED extends WorkerState
case object WORKER_DONE extends WorkerState
