import Core._

import Core.Record._

import org.scalacheck._
import Prop.forAll
import scala.io.Source
import java.io.PrintWriter

object IOTest extends Properties("IO") {
  val inputDir = "src/test/resources/input/"
  val outputDir = "src/test/resources/output/"
  val fileNames = Array("block1", "block2", "block3")
  def writeFile(name: String): Unit = {
    val source = Source.fromFile(inputDir ++ name)
    val block = source.getLines()
    val sortedBlock = block.toArray.sorted
    val writer = new PrintWriter(outputDir ++ name)
    sortedBlock.foreach(writer.println)
    writer.close()
  }
  property("Basic file read and write test") = {
    fileNames.map(writeFile(_))
    Prop.all(fileNames.map { s =>
      Prop {
        val sorted = Source.fromFile(inputDir ++ s).getLines().toArray.sorted
        val answer = Source.fromFile(outputDir ++ s).getLines().toArray
        sorted.zip(answer).map(p => p._1 == p._2).foldLeft(true)(_ && _)
      }
    }: _*)
  }
}
