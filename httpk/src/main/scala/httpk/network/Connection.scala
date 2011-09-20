package kmels.uvg.httpk.network

import java.net.ServerSocket
import kmels.uvg.httpk.util.typeAliases._

object Connection {
  def apply(port:Port):ServerSocket = new ServerSocket(port)
}
