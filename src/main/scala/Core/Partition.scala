import Record._

class Partition(val partition: Stream[Record]) {
  def sampling(size: Int): Stream[Record] = {
    partition take size
  }

  def shuffling(start: Key, end: Key): Partition = {
    new Partition(partition.filter(record => start.key <= record.key && record.key <= end.key))
  }
}
