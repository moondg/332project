object Key {
  def convertFromRecord(record: Record): Key = {
    new Key(record.key)
  }
}

class Key(val key: String) extends Ordered[Key] {
  override def compare(that: Key): Int = this.key `compare` that.key
}
