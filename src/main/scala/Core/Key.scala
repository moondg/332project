package Core

object Key {
  def prev(key: Key): Key = {
    def prevRec(k: Array[Byte]): Array[Byte] = {
      k.isEmpty match {
        case true => k
        case _ =>
          if (k.last == 0x00.toByte) prevRec(k.init) :+ 0xff.toByte
          else k.init :+ (k.last - 1).toByte
      }
    }
    new Key(prevRec(key.key))
  }
  def next(key: Key): Key = {
    def nextRec(k: Array[Byte]): Array[Byte] = {
      k.isEmpty match {
        case true => k
        case _ =>
          if (k.last == 0xff.toByte) nextRec(k.init) :+ 0x00.toByte
          else k.init :+ (k.last + 1).toByte
      }
    }
    new Key(nextRec(key.key))
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
