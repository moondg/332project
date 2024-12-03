package Utils

import Network.Network.{IPAddr, Port}
import Core.Key._
import scala.annotation.tailrec
import message.common.KeyRange

object Prelude {
  def getIPAddr(): IPAddr = {
    val raw = java.net.InetAddress.getLocalHost.getAddress
    raw.foldRight("")((byte, acc) => "." ++ byte.toString ++ acc).tail
  }
  def getPort(): Port = {
    new java.net.ServerSocket(0).getLocalPort
  }

  def inputDirParse(args: List[String]): List[String] = {
    @tailrec
    def inputDirParseRecur(
        args: List[String],
        dir: List[String],
        isInputDir: Boolean): List[String] = {
      args match {
        case "-I" :: tail => inputDirParseRecur(tail, dir, true)
        case "-O" :: _ => dir
        case head :: tail =>
          if (isInputDir) inputDirParseRecur(tail, head :: dir, isInputDir)
          else inputDirParseRecur(tail, dir, isInputDir)
        case _ => List()
      }
    }
    inputDirParseRecur(args, Nil, false)
  }
  def parseInputDirs(args: List[String]): List[String] = {
    (args takeWhile (_ != "-O")) drop 3
  }
}
