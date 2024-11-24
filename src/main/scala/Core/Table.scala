package Core

import Key._

import Network.Network.IPAddr
import Network.Network.Port

object Table {}

class Table(val keyRanges: List[KeyRange], val address: List[(IPAddr, Port)]) {
  def find(key: Key): (IPAddr, Port) = {
    keyRanges.zip(address).filter(x => x._1.contains(key)).head._2
  }
  def find(record: Record): (IPAddr, Port) = find(new Key(record.key))

  def keyRange(key: Key): KeyRange = keyRanges.filter(_.contains(key)).head
  def keyRange(record: Record): KeyRange = keyRange(new Key(record.key))
}
