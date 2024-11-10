object Key {
  def getKey(s: String): String = {
    require(s.length == 100, "String length must be 100")
    s take 10
  }
  def getValue(s: String): String = {
    require(s.length == 100, "String length must be 100")
    s drop 10
  }
}
