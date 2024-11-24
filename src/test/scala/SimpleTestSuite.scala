import Key._
import Record._

import org.scalacheck._
import Prop.forAll

object KeyValueSpecification extends Properties("Key") {
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

object RecordSpecification extends Properties("Record") {
  val recordTestCase = Seq(
    new Record(
      "+#P3n-]RE{",
      "000000013492134098751765416901253906213625908615290681234908123490845161258906231479023412"),
    new Record(
      "@(4i_3nc#M",
      "000000981234098333939393339392390233290232390232390233290223902344303434894334893455855853"))
  property("Record ordering") = Prop.all(recordTestCase.sorted == recordTestCase)
}

object PartitionTest extends Properties("Partition") {
  val data = Stream(
    "!@#$%^&*()000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
    "QWERTYUIOP000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002",
    "Z<X>OJWDKm000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003",
    "BMMMMMMMMM000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004",
    "CMMMMMMMMM000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005")
  val partitionTestCase = new Partition(data.map { convertFromString(_) })
  val samplingAnswer = Stream("!@#$%^&*()", "QWERTYUIOP")
  property("Partition internal sampling") =
    Prop.all(partitionTestCase.sampling(2).map { convertFromRecord(_).key } == samplingAnswer)

  val start = new Key("BMMMMMMMMM")
  val end = new Key("QWERTYUIOP")
  val shufflingAnswer = Stream("QWERTYUIOP", "BMMMMMMMMM", "CMMMMMMMMM")
  property("Partition internal shuffling") = {
    Prop.all(partitionTestCase.shuffling(start, end).partition.map { _.key } == shufflingAnswer)
  }
}
