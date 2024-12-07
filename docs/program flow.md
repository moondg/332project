# Program flow

## Initial phase

1. Worker generates data with **gensort**.
1. Master knows how many workers will participate.
1. Worker knows master's IP address and port number.

## Register phase

1. Worker sends `EstablishmentRequest` to master.
1. Worker registers to master's server and master send `EstablishmentResponse` to worker.
1. Worker receives `EstablishmentResponse`.

## Sampling phase

1. If all worker registered, master sends `SamplingRequest` to every worker.
1. Worker receives `SamplingRequest`.
1. Worker collects *core-many* sample set. Sample set is just `take n` function.
1. Worker sorts sample set into single sample and sends it to master with `SamplingResponse`.
1. Master collects every sample from worker and merge sort sample sets.

## Sorting/Partition phase

1. Master makes key range table and broadcasts to workers. Table includes
    + Worker's key range
    + Worker's IP address
    + Worker's Port number
1. Master sends `PartitioningRequest` to all workers.
1. Worker gets its own key range.
1. Worker splits its disk into block(this can be done in [Initial phase](#initial-phase)). Block size is approximately same as RAM size, but should not exceed.
1. Worker spilts block into several partitions using key range table and save as partition file
1. Worker sends `PartitionResponse` to master.
1. Master waits until every worker sends `PartitionResponse`.

## Shuffling phase

1. Master sends `ShuffleRequest` to every workers.
1. Worker opens two port: sending and receiving. Sending sends *Not My Partition* part to other proper worker. Receiving receives *Yes My Partition* part from other workers.
1. Worker shuffles partition concurrently.(Master does nothing here)
1. For each worker, if it received all partition of itself, sends `ShuffleResponse` to master.

## Merging phase

1. For each worker, whenever master received `ShuffleResponse`, send worker to `MergeRequest`.
1. Worker receives `MergeRequest`.
1. Worker starts internal k-way merge sort using received partitions. Tournament tree will be used here.
1. After merge finished, worker sends `MergeResponse` to master.

## Verification phase(Extra)

1. Master sends `VerificationRequest` to every worker.
1. Worker receives `VerificationRequest`.
1. Worker iterates every data and checks whether data is strictly increasing or not.
1. Worker sends this Boolean value `isOrdered` with its minimum/maximum data to master. Send `VerificationResponse`.
1. Master waits until every worker sends `VerificationResponse`.
1. Master folds `List[isOrdered]` and checks every min/max data is valid.