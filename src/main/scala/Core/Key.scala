package Core

object Key {
  type Key = String

  class KeyRange(val start: Key, val end: Key) {
    def contains(key: Key): Boolean = {
      (start == "MINIMUM", end == "MAXIMUM") match {
        case (true, true) => true
        case (true, false) => key <= end
        case (false, true) => start <= key
        case (false, false) => start <= key && key <= end
      }
    }
    def contains(record: Record): Boolean = {
      contains(record.key)
    }
  }
}
