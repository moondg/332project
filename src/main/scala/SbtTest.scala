object SbtTest {
  def formTriangle(x: Int, y: Int, z: Int): Boolean = {
    val l = List(x, y, z).sorted
    l.head + l.tail.head > l.tail.tail.head
  }

  // retrun greatest common divisor
  def gcd(a: Int, b: Int): Int = {
    require(0 != a && 0 != b, "a and b must be nonzero")
    (a % b) == 0 match {
      case true => b
      case false => gcd(b, a % b)
    }
  }

  // return Euler phi function
  def eulerPhiFun(n: Int): Int = {
    require(0 < n, "n must be positive")
    (1 to n).filter(gcd(n, _) == 1).length
  }

  // checking https://en.wikipedia.org/wiki/Euler%27s_totient_function#Divisor_sum
  def needToProof(n: Int): Int = {
    require(0 < n, "n must be positive")
    (1 to n).filter(n % _ == 0).map(eulerPhiFun(_)).sum
  }
}
