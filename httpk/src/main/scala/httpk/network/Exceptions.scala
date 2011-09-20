package kmels.uvg.httpk.network.exceptions

/**
 * Exceptions for the network package
 * @author Carlos Lopez Camey
 */

abstract class NetworkException extends Exception{
  val msg:String
  override def toString = msg
}

case class PortAlreadyInUse(val port:Int) extends NetworkException{
  val msg = "Port already in use: "+port
}

