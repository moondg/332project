object Record {
  def getKey(s: String): String = {
    require(s.length == 100, "String length must be 100")
    s take 10
  }
  def getValue(s: String): String = {
    require(s.length == 100, "String length must be 100")
    s drop 10
  }
  def convertFromString(str: String): Record = {
    require(str.length() == 100, "convertFromString: length of String is not 100")
    new Record(key = getKey(str), value = getValue(str))
  }
}

class Record(val key: String, val value: String) extends Ordered[Record] {
  override def compare(that: Record): Int = this.key `compare` that.key
}
