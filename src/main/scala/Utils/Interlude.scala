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
}
