package Core

import Key._
import Record._
import scala.io.Source
import Utils.Interlude.readFile

object Block {
  def sampling(filePath: String, n: Int): List[Key] = {
    import scala.io.Source
    val source = Source.fromFile(filePath, "ISO8859-1")
    val size = Constant.Size.record * n
    try {
      (source take size)
        .grouped(Constant.Size.record)
        .map(rawRecord => rawRecord take Constant.Size.key)
        .map(rawKey => new Key(rawKey.map(_.toByte).toArray))
        .toList
    } finally {
      source.close()
    }
  }
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
