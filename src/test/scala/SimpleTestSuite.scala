import Core._

import Core.Record._

import org.scalacheck._
import Prop.forAll
import scala.io.Source
import java.io.PrintWriter

object ParsingTest extends Properties("Parsing") {
  val keyParsingTestCase = Seq(
    (
      "@(4i_3nc#M000000981234098333939393339392390233290232390232390233290223902344303434894334893455855853",
      "@(4i_3nc#M"),
    (
      "+#P3n-]RE{000000013492134098751765416901253906213625908615290681234908123490845161258906231479023412",
      "+#P3n-]RE{"))
  property("Key parsing") = Prop.all(keyParsingTestCase.map { case (input, output) =>
    Prop { getKey(input) == output }
  }: _*)

  val valueParsingTestCase = Seq(
    (
      "@(4i_3nc#M000000981234098333939393339392390233290232390232390233290223902344303434894334893455855853",
      "000000981234098333939393339392390233290232390232390233290223902344303434894334893455855853"),
    (
      "+#P3n-]RE{000000013492134098751765416901253906213625908615290681234908123490845161258906231479023412",
      "000000013492134098751765416901253906213625908615290681234908123490845161258906231479023412"))
  property("Value parsing") = Prop.all(valueParsingTestCase.map { case (input, output) =>
    Prop { getValue(input) == output }
  }: _*)
}

object RecordTest extends Properties("Record") {
  val recordTestCase = Seq(
    new Record(
      "+#P3n-]RE{",
      "000000013492134098751765416901253906213625908615290681234908123490845161258906231479023412"),
    new Record(
      "@(4i_3nc#M",
      "000000981234098333939393339392390233290232390232390233290223902344303434894334893455855853"))
  property("Record ordering") = Prop.all(recordTestCase.sorted == recordTestCase)
}

object BlockTest extends Properties("Block") {
  val data = Stream(
    "!@#$%^&*()000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
    "QWERTYUIOP000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002",
    "Z<X>OJWDKm000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003",
    "BMMMMMMMMM000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004",
    "CMMMMMMMMM000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005")
  val blockTestCase = new Block(data.map { convertFromString(_) })
  val samplingAnswer = Stream("!@#$%^&*()", "QWERTYUIOP")
  property("Block internal sampling") =
    Prop.all(blockTestCase.sampling(2).map { _.key } == samplingAnswer)
}

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
