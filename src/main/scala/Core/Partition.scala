import Record._

class Partition(val partition: Stream[Record]) {
  def sampling(size: Int): Stream[Record] = {
    partition take size
  }
}
