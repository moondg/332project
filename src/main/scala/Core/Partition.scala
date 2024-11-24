package Core

import Key._
import Record._
import Network.Network.IPAddr
import Network.Network.Port

class Partition(val partition: Stream[Record], val worker: (IPAddr, Port)) {
  def shuffling = ???
}
