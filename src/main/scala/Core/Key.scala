package Core

object Key {
  def prev(key: Key): Key = {
    new Key(key.key.init :+ (key.key.last - 1).toByte)
  }
  def max: Key = new Key(Array.fill(Core.Constant.Size.key) { 255.toByte })
  def min: Key = new Key(Array.fill(Core.Constant.Size.key) { 0.toByte })
}

class Key(val key: Array[Byte]) extends Ordered[Key] {
  override def compare(that: Key): Int = {
    this.key.zip(that.key).map { case (l, r) => l.toChar - r.toChar }.find(_ != 0).getOrElse(0)
  }
}

class KeyRange(val start: Key, val end: Key) {
  def contains(key: Key): Boolean = {
    start <= key && key <= end
  }
  def contains(record: Record): Boolean = {
    contains(record.key)
  }
}
