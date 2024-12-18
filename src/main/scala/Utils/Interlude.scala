package Utils

import Core._
import Core.Key._
import Core.Record._
import Core.Table._

import Network.Network.Node

object Interlude {
  def dividePartition(records: List[Record], table: Table): List[(List[Record], Node)] = {
    table match {
      case Nil => List()
      case (keyRange, node) :: next => {
        keyRange.end == Key.max match {
          case true => List((records, node))
          case false => {
            val (sending, remaining) = records span (keyRange.contains)
            (sending, node) :: dividePartition(remaining, next)
          }
        }
      }
    }
  }
  def readFile(filePath: String): Array[Byte] = {
    import scala.io.Source
    val source = Source.fromFile(filePath, "ISO8859-1")
    try {
      source.map(_.toByte).toArray
    } finally {
      source.close()
    }
  }
  def writeFile(filePath: String, data: List[Record]): Unit = {
    import java.io.{BufferedOutputStream, FileOutputStream}
    val outputStream = new BufferedOutputStream(new FileOutputStream(filePath))
    try {
      data.foreach(record => outputStream.write(record.raw))
    } finally {
      outputStream.close()
    }
  }
  def writeFile(filePath: String, data: Array[Byte]): Unit = {
    import java.io.{BufferedOutputStream, FileOutputStream}
    val outputStream = new BufferedOutputStream(new FileOutputStream(filePath))
    try {
      outputStream.write(data)
    } finally {
      outputStream.close()
    }
  }
}
