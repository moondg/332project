package Core

import Key._
import Record._
import scala.io.Source
import Utils.Interlude.readFile

object Block {
  def makeBlockFromFile(filePath: String): Block = {
    new Block(for {
      forType <- List()
      arr <- readFile(filePath).grouped(Core.Constant.Size.record)
    } yield convertFrom(arr))
  }
}

class Block(val block: List[Record]) {
  def size: Int = block.length
  def raw: Array[Byte] = block.flatMap(record => record.raw).toArray
  def sampling(size: Int): List[Key] = {
    (block take size).map(_.key)
  }
}
