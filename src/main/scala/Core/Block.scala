package Core

import Key._
import Record._
import scala.io.Source

object Block {
  def makeBlockFromFile(fileName: String): Block = {
    new Block(
      Source
        .fromFile(fileName, "ISO8859-1")
        .map(_.toByte)
        .toArray
        .grouped(Core.Constant.Size.record)
        .map(byteArr => convertFrom(byteArr))
        .toList)
  }
}

class Block(val block: List[Record]) {
  def sampling(size: Int): List[Key] = {
    (block take size).map(_.key)
  }
}
