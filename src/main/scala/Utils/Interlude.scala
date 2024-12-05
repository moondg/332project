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
        val (sending, remaining) = records span (keyRange.contains)
        (sending, node) :: dividePartition(remaining, next)
      }
    }
  }
  def readFile(filePath: String): Array[Byte] = {
    import java.io.{BufferedInputStream, FileInputStream}
    val inputStream = new BufferedInputStream(new FileInputStream(filePath))
    try {
      inputStream.readAllBytes()
    } finally {
      inputStream.close()
    }
  }
  def writeFile(filePath: String, data: List[Record]): Unit = {
    writeFile(filePath, data.flatMap(_.raw).toArray)
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
