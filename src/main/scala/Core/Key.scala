package Core

object Key {
  type Key = String

  class KeyRange(val start: Key, val end: Key) {
    def contains(key: Key): Boolean = {
      start <= key && key <= end
    }
    def contains(record: Record): Boolean = {
      contains(record.key)
    }
  }
}
