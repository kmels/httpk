package kmels.uvg.httpk.network

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import com.weiglewilczek.slf4s.Logging
import actors.Actor._
import actors.Futures._
import kmels.uvg.httpk.util.{typeAliases,Settings}
import typeAliases._
import exceptions._
import java.io.IOException

class Servlet(val settings:Settings) extends Logging{
  val service = new Service(settings)
  service.start
  
  private def getConnection(port:Port):Option[Connection] = try {
    logger debug "Trying to get a connection on port "+port
    Some(Connection(port))
  } catch {
    case portInUse:IOException => {
      logger error "Could not get a connection on port "+port
      None
    }
  }
  
  
  val connections:Seq[Connection] = settings.ports.map(getConnection(_)).flatten
  
  def listen:Unit = 
    if (!connections.isEmpty)
      doListen(connections)
    else{
      logger.error("Could not get a connection in any of the following ports "+settings.ports.mkString(","))
      throw new PortAlreadyInUse(settings.ports.head)
    }
  
  private def doListen(connections:Seq[Connection]):Unit = connections foreach(
    connection => 
      future {
	logger.info("Listening on port "+connection.getLocalPort)
	while(true) {
	  logger debug "About to block in "+connection
	  val clientSocket = connection.accept
	  service ! clientSocket
	  logger.debug("Back from service")
	}
      }
  )
}
