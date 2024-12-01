package Core

import Key._

import Network.Network.Node

object Table {
  type Table = List[(KeyRange, Node)]
}
