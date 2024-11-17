object Record {
  def convertFromString(str: String): Record = {
    require(str.length() == 100, "convertFromString: length of String is not 100")
    new Record(key = str.getKey, value = str.getValue)
  }
}
class Record(val key: String, val value: String) extends Ordered[Record] {
  override def compare(that: Record): Int = this.key `compare` that.key
}
