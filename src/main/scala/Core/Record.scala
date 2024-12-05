package Core

import Key._

object Record {
  def getKey(s: Array[Byte]): Key = new Key(s take Core.Constant.Size.key)
  def getValue(s: Array[Byte]): Array[Byte] = s drop Core.Constant.Size.key

  def convertFrom(arr: Array[Byte]): Record = {
    new Record(key = getKey(arr), value = getValue(arr))
  }
}

class Record(val key: Key, val value: Array[Byte]) extends Ordered[Record] {
  def raw: Array[Byte] = key.key ++ value
  override def compare(that: Record): Int = this.key `compare` that.key
}
