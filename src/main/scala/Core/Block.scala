package Core

import Key._
import Record._
import scala.io.Source
import Utils.Interlude.readFile

object Block {
  def makeBlockFromFile(filePath: String): Block = {
    new Block(
      readFile(filePath)
        .grouped(Constant.Size.record)
        .map(arr => convertFrom(arr))
        .toList)
  }
}

class Block(val block: List[Record]) {
  def size: Int = block.length
  def raw: Array[Byte] = block.flatMap(record => record.raw).toArray
  def sampling(size: Int): List[Key] = {
    (block take size).map(_.key)
  }
}
