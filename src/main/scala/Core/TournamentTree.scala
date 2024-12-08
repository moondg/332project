package Core

import Core.Record._
import scala.io.Source
import Core.TournamentTree._

object TournamentTree {
  sealed trait Tree[A <: Ordered[A]] {
    var value: Option[A]
    def update(force: Boolean): Unit
  }
  case class Leaf() extends Tree[Record] {
    var value: Option[Record] = None
    def update(force: Boolean): Unit = ()
  }
  case class Fruit(filePath: String) extends Tree[Record] {
    val source = Source.fromFile(filePath, "ISO8859-1")
    var value: Option[Record] = None
    def update(force: Boolean) = {
      if (source.isEmpty) value = None
      else value = Some(recordFrom(source.take(Constant.Size.record).map(_.toByte).toArray))
    }
  }
  case class Node(left: Tree[Record], right: Tree[Record]) extends Tree[Record] {
    var value: Option[Record] = None
    def update(force: Boolean) = {
      if (force == true) {
        left.update(force = true)
        right.update(force = true)
      } else {
        (left.value, right.value) match {
          case (None, None) => ()
          case (Some(_), None) => left.update(force = false)
          case (None, Some(_)) => right.update(force = false)
          case (Some(x), Some(y)) =>
            if (x < y) left.update(force = false)
            else right.update(force = false)
        }
      }
      value = Record.max(left.value, right.value)
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
  def isEmpty = tree.value.isEmpty
  def nonEmpty = tree.value.nonEmpty

  def merge(): Unit = {
    val file = new File(outputFilePath)
    val sink = new FileOutputStream(file)
    tree.update(force = true)
    while (tree.value.isDefined) {
      sink.write(tree.value.get.raw)
      tree.update(force = false)
    }
    sink.close()
  }
}
