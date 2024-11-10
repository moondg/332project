// object Record {}
class Record(val key: String, val value: String) extends Ordered[Record] {
  override def compare(that: Record): Int = this.key `compare` that.key
}
