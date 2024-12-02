import org.scalacheck._
import Prop.forAll
import Utils.Prelude._

object ParseTest extends Properties("Prelude") {
  val args = List(
    List(
      "worker",
      "1.22.1.1:1557",
      "-I",
      "data/small/partition1",
      "data/small/partition2",
      "data/small/partition3",
      "-O",
      "output"),
    List(
      "worker",
      "1.22.1.1:1557",
      "-I",
      "data/big/block1",
      "data/big/block2",
      "data/big/block3",
      "-O",
      "output"))
  val answer =
    List(
      List("data/small/partition1", "data/small/partition2", "data/small/partition3"),
      List("data/big/block1", "data/big/block2", "data/big/block3"))
  property("Parse Input Directory from argument") =
    Prop.all(args.zip(answer).map { case (input, output) =>
      Prop { parseInputDirs(input) == output }
    }: _*)
}
