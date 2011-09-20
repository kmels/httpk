package kmels.uvg.httpk.util

/**
 * Settings for Httpk
 * @author Carlos Lopez-Camey
 * @since 2.0
 */
import com.weiglewilczek.slf4s.Logging
import kmels.uvg.httpk.{service,util}
import service._
import util.typeAliases._
import util.typeConversions._
import scala.xml.{Elem,NodeSeq,XML,Node}

class Settings(val ports:Seq[Port], val numberOfThreads:Int, val virtualHosts:Seq[VirtualHost], val distributedHosts:Seq[DistributedHost],val defaultErrorResponseDirectory:String)

object Settings extends Logging{
  def apply(pathToFile:String):Option[Settings] = {    
    logger.debug("Loading configuration from file: "+pathToFile)
    val xmlRoot:Elem = XML.loadFile(new java.io.File(pathToFile))

    val defaultErrorResponseDirectory:String = xmlRoot \\ "default-errors-directory" text

    val ports:Seq[Port] = xmlRoot \ "ports" \\ "port" map (_.text.toInt)
    logger.info("Found listening ports: "+ports.mkString(","))

    val nthreads:Int = (xmlRoot \\ "ThreadPool" \\ "@number").text.toInt
    logger.info("Found thread pool size: "+nthreads)
    
    val vhosts:Seq[VirtualHost] = (xmlRoot \ "VirtualHosts" \\ "VirtualHost").map(virtualHostBuilder)
    logger.info("Found "+vhosts.size+" virtual hosts")

    val distributedHosts:Seq[DistributedHost] = (xmlRoot \ "DistributedHosts" \\ "DistributedHost").map(distributedHostBuilder)

    Some(new Settings(ports,nthreads,vhosts,distributedHosts,defaultErrorResponseDirectory))
  }

  private val distributedHostBuilder: NodeSeq => DistributedHost = dHostNode => {
    val name:String = dHostNode \\ "@name" text
    val ip:String = dHostNode \\ "@ip" text    
    val port:Int = (dHostNode \\ "@port" text).toInt
    val priority:Int = (dHostNode \\ "@priority" text) toInt
    
    DistributedHost(name,ip,port,priority)
  }

  private val virtualHostBuilder: NodeSeq => VirtualHost = vhostNode => {
    val name:String = vhostNode \\ "@name" text

    //logger.debug("Building virtual host: "+name)

    val listeningPorts:Option[List[Int]] = vhostNode \\ "@listenOn" text match{
      case "*" => None
      case ports => Some(ports.split(",").map(_.toInt).toList)
    }

    val documentRoot:String = vhostNode \\ "DocumentRoot" text
    val handlersNode = vhostNode \ "Handlers"
    val handlers:Seq[Handler] = (handlersNode \\ "Error" ++ handlersNode \\ "Match" ++ handlersNode \\ "Auth") map handlerBuilder(documentRoot)
    val optionsNode = vhostNode \ "Options"
    val options:Seq[HttpkOption] = (optionsNode \\ "Indexes" ++ optionsNode \\ "FollowSymLinks") map optionBuilder

    val portsString = listeningPorts match {
      case Some(portsList) => "ports "+portsList.mkString(",")
      case _ => "any port"
    }
    logger.info("Virtual host \""+name+"\" will be listening on "+portsString+" with document root at "+documentRoot+". It has "+handlers.size+" has Handlers"+" and "+options.size+" Options")
    VirtualHost(name,listeningPorts,documentRoot,handlers,options)
  }

  private def handlerBuilder(pathToDirectory:String): NodeSeq => Handler = handlerNode => 
    handlerNode match{
      case <Error>{pathToFile}</Error> => ErrorHandler((handlerNode \\ "@number").text.toInt,pathToDirectory+pathToFile.text)
      case <Auth>{pathToFile}</Auth> => AuthHandler((handlerNode \\ "@regex").text,pathToDirectory+pathToFile.text)
      case <Match>{pathToFile}</Match> => MatchHandler((handlerNode \\ "@regex").text,pathToDirectory+pathToFile.text)
    }

  private val optionBuilder: NodeSeq => HttpkOption = optionNode => {
    //to do
    implicit def nodeToBool(x:Node):Boolean = x.text.toBoolean

    optionNode match{
      case <Indexes>{allow}</Indexes> => IndexesOption(allow)
      case <FollowSymLinks>{allow}</FollowSymLinks> => FollowSymLinks(allow)
    }
  }
}

