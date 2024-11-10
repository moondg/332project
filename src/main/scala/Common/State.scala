package Common

sealed trait MasterState
case object MASTER_INITIAL extends MasterState

sealed trait WorkerState
case object WORKER_INITIAL extends WorkerState
case object WORKER_DONE extends WorkerState
