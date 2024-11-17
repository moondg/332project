import Key._
// import Record._
import Partition._

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
  lazy val partitionTestCase = Partition(
    Stream(
      "!@#$%^&*()000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001",
      "QWERTYUIOP000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002",
      "Z<X>OJWDKm000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003"))
  val samplingAnswer = Stream("!@#$%^&*()", "QWERTYUIOP", "Z<X>OJWDKm")
  property("Partition sampling") = Prop.all(partitionTestCase.sampling() == samplingAnswer)
}
