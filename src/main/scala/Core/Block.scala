package Core

import Key._
import Record._

object Block {}

class Block(val block: Stream[Record]) {
  def sampling(size: Int): Stream[Record] = {
    block take size
  }
  def partitioning(keyRange: KeyRange): Stream[Record] = {
    block.takeWhile(record => keyRange.contains(record.key))
  }
}
