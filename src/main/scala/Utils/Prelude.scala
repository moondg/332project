package Utils

import Network.Network.IPAddr

object Prelude {
  def getIPAddr(): IPAddr = {
    import java.net.InetAddress
    val raw = InetAddress.getLocalHost.getAddress
    raw.foldRight("")((byte, acc) => "." ++ byte.toString ++ acc).tail
  }
}
