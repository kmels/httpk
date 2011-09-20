package kmels.uvg.httpk.network

/**
 * Service that receives requests
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.service.{http,Dispatcher,VirtualHost,RequestHandler,DistributedRequestHandler,DistributedHost}
import http.{HttpResponse,HttpRequest,DistributedHttpRequest}
import kmels.uvg.httpk.util.Settings
import kmels.uvg.httpk.util.IO._
import kmels.uvg.httpk.util.typeAliases._
import Dispatcher._
import scala.actors.{Actor,IScheduler,Scheduler}
import Actor._
import java.io.{InputStream,OutputStream,PrintWriter,OutputStreamWriter}
import java.net.Socket
import com.weiglewilczek.slf4s.Logging

class Service(settings:Settings) extends Actor with Logging{
  implicit val aScheduler:IScheduler = Scheduler//.makeNewScheduler 
  
  implicit def outputStreamWrapper(out: OutputStream) = new PrintWriter(new OutputStreamWriter(out))
  
  implicit def httpResponseToHTML(response:HttpResponse):String = response.mkString
  
  def getDistributedHost(distributedRequest:DistributedHttpRequest):Option[DistributedHost] = {
    val dHostByName = settings.distributedHosts.find(_.name == distributedRequest.distributedHostName)
    
    dHostByName match{
      case Some(distributedHost) => dHostByName
      case _ => {
	logger.warn("There's no distributed name with name "+distributedRequest.distributedHostName+" contained in "+settings.distributedHosts.map(_.name).mkString(","))
	None
      }
    }
  }

  def getDistributedCoordinatorHost:DistributedHost = settings.distributedHosts.find(_.isCoordinator).get

  def getVirtualHost(request:HttpRequest):Option[VirtualHost] = {
    val vHostByName = settings.virtualHosts.find(_.name == request.hostName)    
    vHostByName match{
      case Some(vHost) => vHost.listeningPorts match{
	case Some(listeningPorts) => 
	  if (listeningPorts.contains(request.port)){
	    logger.info("found request's virtual host owner: "+vHost.name)
	    vHostByName
	  }	    
	  else{
	    logger.warn("request.port "+request.port+" is not contained within vHosts listening ports: "+listeningPorts.mkString(","))
	    None
	  }	    
	case _ => {
	  logger.info("found request's virtual host owner: "+vHost.name)
	  vHostByName
	}
      }
      case _ => {
	logger.warn("request.hostName "+request.hostName+" does not match any vHost name: "+settings.virtualHosts.mkString(","))
	None
      }
    }
  }

  import java.util.Date
  def act() {
    loop {
      receive {
	case socket:Socket => 
	  //TODO: dispatch for thread pooling
	  actor{
	    logger info new Date +", received socket: inetAddress="+socket.getInetAddress+" port="+socket.getLocalPort	    
	      
	    val request:Option[HttpRequest] = parseRequest(socket.getInputStream)
	    request match{
	      case Some(request) => {
		logger info "Request: "+request	      

		val httpResponse:HttpResponse = request.toDistributedRequest(settings.distributedHosts) match{
		  case Some(distributedRequest) => {
		    val handler = DistributedRequestHandler(getDistributedHost(distributedRequest),getDistributedCoordinatorHost)
		    val httpResponse:HttpResponse = handler(distributedRequest)
		    httpResponse
		  }
		  case _ => {		    
		    val handler = RequestHandler(getVirtualHost(request))
		    val httpResponse:HttpResponse = handler(request)
		    httpResponse
		  }		    
		}
		
		val response:String = httpResponse
		val clientResponse:PrintWriter = socket.getOutputStream
		logger info "HTTP Status Response: "+httpResponse.status
		logger info "HTTP Transferred bytes: "+httpResponse.html().size
		clientResponse.println(response)
		clientResponse.flush
		socket.close
	      }
	      case _ => {
		println("NO REQUEST!!!!")
	      }
	    }
	  } //case end socket
	case wtf => println("recibio "+wtf)
      }
    }  
  }
}
