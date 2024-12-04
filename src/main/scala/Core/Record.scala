package Core

import Key._

object Record {
  def getKey(s: Array[Byte]): Key = new Key(s take 10)
  def getValue(s: Array[Byte]): Array[Byte] = s drop 10

  def convertFrom(arr: Array[Byte]): Record = {
    new Record(key = getKey(arr), value = getValue(arr))
  }
}

class Record(val key: Key, val value: Array[Byte]) extends Ordered[Record] {
  override def compare(that: Record): Int = this.compare(that)
}
