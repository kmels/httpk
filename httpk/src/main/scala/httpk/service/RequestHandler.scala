package kmels.uvg.httpk.service	    

import kmels.uvg.httpk.{util,network}
import http.HttpErrorCodes._
import http.HttpSuccessStatus._
import http.{HttpRequest,HttpResponse,DefaultErrorResponse}
import util.typeAliases._
import util.typeConversions._
import com.weiglewilczek.slf4s.Logging

class RequestHandler(val vHost:VirtualHost) extends (HttpRequest => HttpResponse) with Logging{
  def apply(request: HttpRequest):HttpResponse = {
    findUserRegexHandler(request) match{
      case Some(matchedHandler) => {
	logger info "Found user match handler: "+matchedHandler.getClass.getName    
	
	//TODO: cache for user handlers	
	matchedHandler match{
	  case authHandler:AuthHandler => if (authHandler.authenticates(request)) handle(request) else authHandler()
	  case handler => handler()
	}
      }
      case _ => {
	logger info "Handling request normally"
	val pathToFile = vHost.documentRoot+request.relativeURL	
	CacheHandler(pathToFile) match{
	  case Some(cacheResponse) => cacheResponse
	  case _ => handle(request)
	}
      }
    }
  }

  object CacheHandler {
    def apply(pathToFile:String):Option[HttpResponse] = {
      logger info "checking for cached file"
      vHost.cache(pathToFile) match{
	case Some(bytes) => {
	  logger info "found cached file"
	  Some(HttpResponse.fromBytes(bytes))
	}
	case _ => {
	  logger info "could not found cached file"
	  None
	}
      }
    }
  }
  /**
   * Handles a request for this virtual host
   */
  private def handle(request:HttpRequest,extraCode: => Unit = {}):HttpResponse = {
    val pathToRequestedFile = vHost.documentRoot+request.relativeURL
    logger debug "Requested file: "+pathToRequestedFile

    val response = HttpResponse.fromFile(pathToRequestedFile)     
    response.status match{
      case errorStatus:HttpErrorStatus => handleError(errorStatus)
      case success:HttpSuccessStatus => {
	logger info "caching file "+pathToRequestedFile
	vHost.encache(pathToRequestedFile,HTMLToBytes(response.html()))
	extraCode
	response
      }
    }
  }

  private def handleError(errorStatus:HttpErrorStatus):HttpResponse = {
    logger info "Handling error status: "+errorStatus
    
    val errorHandler = vHost.errorHandlers.find(_.errorStatus.statusCode == errorStatus.statusCode)

    errorHandler match {
      case Some(handler) => {
	logger info "Found user error handler "+handler
	handler()
      }
      case _ => errorStatus match{    
	case x => DefaultErrorResponse(errorStatus)
      }
    }  
  }

  /**
   * Gets a MatchHandler from the settings
   */
  private def findUserRegexHandler(request:HttpRequest):Option[RegexHandler] = vHost.regexHandlers.find(_.matchesRequest(request))

}

object RequestHandler {
  def apply(virtualHost:Option[VirtualHost]):HttpRequest => HttpResponse = virtualHost match{
    case Some(vHost) => new RequestHandler(vHost)
    case noVHost => req => DefaultErrorResponse(E422)
  }
}
