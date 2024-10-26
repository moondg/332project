// test case for command `sbt test`
// you may delete this file

import SbtTest._

import org.scalacheck._
import Prop.forAll

object SbtTestSpecification extends Properties("SbtTest") {
  property("Does gcd divides given integer") = forAll { (a: Int, b: Int) =>
    Prop(0 != a && 0 != b) ==> ((a % gcd(a, b) == 0) && (b % gcd(a, b) == 0))
  }

  val positiveIntGen = Gen.posNum[Int]
  property("Check match with n") = forAll(positiveIntGen) { x =>
    (needToProof(x) == x)
  }

  val triangleTestCase = Seq((3, 4, 5), (2, 2, 3), (1, 1, 1), (10, 14, 23))
  property("Check 3 integer can form triangle") = Prop.all(triangleTestCase.map {
    case (x, y, z) =>
      Prop { formTriangle(x, y, z) == true }
  }: _*)
}
