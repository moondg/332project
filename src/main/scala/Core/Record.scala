package Core

import Key._

object Record {
  def getKey(s: String): Key = s take 10
  def getValue(s: String): String = s drop 10

  def convertFromString(str: String): Record = {
    new Record(key = getKey(str), value = getValue(str))
  }
}

class Record(val key: Key, val value: String) extends Ordered[Record] {
  override def compare(that: Record): Int = this.key `compare` that.key
}
