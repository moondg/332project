package Core

import Core.Record._
import scala.io.Source
import Core.TournamentTree._

object TournamentTree {
  sealed trait Tree[A <: Ordered[A]] {
    var value: Option[A]
    def pick: Option[A]
    def initialize: Unit
  }
  case class Leaf() extends Tree[Record] {
    var value: Option[Record] = None
    def pick: Option[Record] = None
    def initialize: Unit = ()
  }
  case class Fruit(filePath: String) extends Tree[Record] {
    val source = Source.fromFile(filePath, "ISO8859-1")
    var value: Option[Record] = None
    def initialize = {
      if (source.isEmpty) value = None
      else value = Some(recordFrom(source.take(Constant.Size.record).map(_.toByte).toArray))
    }
    def pick = {
      if (source.isEmpty) value = None
      else value = Some(recordFrom(source.take(Constant.Size.record).map(_.toByte).toArray))
      value
    }
  }
  case class Node(left: Tree[Record], right: Tree[Record]) extends Tree[Record] {
    var value: Option[Record] = None
    def initialize = {
      left.initialize
      right.initialize
      value = Record.max(left.value, right.value)
    }
    def pick = {
      (left.value, right.value) match {
        case (None, None) => value = None
        case (Some(x), None) => value = left.pick
        case (None, Some(y)) => value = right.pick
        case (Some(x), Some(y)) => {
          if (x < y) value = Record.max(left.pick, right.value)
          else value = Record.max(left.value, right.pick)
        }
      }
      value
    }
  }

  def gardener(leaves: List[String]): Tree[Record] = {
    leaves match {
      case Nil => Leaf()
      case head :: Nil => Fruit(head)
      case _ => {
        val (left, right) = leaves splitAt (leaves.length / 2)
        Node(gardener(left), gardener(right))
      }
    }
  }
}

class TournamentTree(filePaths: List[String], outputFilePath: String) {
  import java.io.{File, FileOutputStream}
  val tree = gardener(filePaths)
  val initialize: Unit = tree.initialize
  def isEmpty = tree.value.isEmpty
  def nonEmpty = tree.value.nonEmpty
  val file = new File(outputFilePath)
  val sink = new FileOutputStream(file, file.exists())

  def merge(): Unit = {
    sink.write(tree.value.get.raw)
    scala.util.control.Breaks.breakable {
      while (true) {
        val v = tree.pick
        if (v.isEmpty) {
          scala.util.control.Breaks.break()
        } else sink.write(v.get.raw)
      }
    }
    sink.close()
  }
}
