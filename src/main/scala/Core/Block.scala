package Core

import Key._
import Record._
import scala.io.Source

object Block {
  def makeBlockFromFile(fileName: String): Block = {
    new Block(Source.fromFile(fileName).getLines().map(convertFromString(_)).toList)
  }
}

class Block(val block: List[Record]) {
  def sampling(size: Int): List[Key] = {
    (block take size).map(_.key)
  }
  def partitioning(keyRange: KeyRange): List[Record] = {
    block.takeWhile(record => keyRange.contains(record.key))
  }
}
