package kmels.uvg.httpk.service

import kmels.uvg.httpk.{util,network}
import kmels.uvg.httpk.service.http.{UserManagementRequest,HttpRequest,HttpResponse,DistributedHttpRequest}
import com.weiglewilczek.slf4s.Logging
import http._
import UserManagementActionType._
import DowntimeActionType._
import HttpErrorCodes._
import http.DefaultErrorResponse
import kmels.uvg.httpk.util.typeConversions._

class DistributedRequestHandler(val distributedHost:DistributedHost,val coordinator:DistributedHost) extends (DistributedHttpRequest => HttpResponse) with Logging{
  def apply(request: DistributedHttpRequest):HttpResponse = {    
    logger info "Handling distributed request"

    request match{
      case UserManagementRequest(action,username,password,distributedHostName) => {
	logger info "catched user management request"
	//are we coordinators?		
	if (distributedHost.isThisHost && distributedHost.isCoordinator)
	  handleUserManagementRequest(action,username,password)
	else{
	  HttpResponse.fromURL(distributedHost.buildURL(userManagementActionToString(action)+"/"+username+"/"+password)) //send to coordinator
	}
      }
      case DistributedUserAuthRequest(username,password,hostName,uri) => {
	logger info "catched user validation request"
	HttpResponse.fromURL(distributedHost.buildURL(uri,Some((username,password))))
      }
      case UserValidationRequest(auth,_) => {
	logger info "User validation request"
	//are we coordinators?
	if (distributedHost.isThisHost && distributedHost.isCoordinator)
	  handleUserValidationRequest(auth)
	else
	  HttpResponse.fromURL(coordinator.buildURL("authenticate",auth))
      }
      case downtimeRequest:DowntimeRequest => handleDowntime(downtimeRequest)
      case DistributedFileRequest(hostname,uri) => {
	logger info "Distributed file request: "+request
	uri.endsWith("htmls") match {
	  case true => {
	    //needs auth
	    logger info "file needs auth"
	    BasicAuthResponse(HttpErrorCodes.E401,() => "")
	  }
	  case _ => {
	    //normal distributed request
	    logger info "normal file request"
	    HttpResponse.fromURL(distributedHost.buildURL(uri))
	  }
	}	      	
      }
      case _ => {
	logger error "Received: "+request+"; required: DistributedRequest"
	null
      }
    }
  }

  def handleUserValidationRequest(auth:Option[(String,String)]):HttpResponse = UserManagement.authenticate(auth) match {
    case true => SimpleHttpResponse(HttpSuccessStatus.S200,() => "")
    case _  => HttpError(HttpErrorCodes.E403)
  }

  def handleUserManagementRequest(action:UserManagementActionType, username:String, password:String):HttpResponse = action match{
    case CREATE => UserManagement.addUser(username,password)
    case EDIT => UserManagement.editUser(username,password)
    case DELETE => UserManagement.deleteUser(username)
  }

  def handleDowntime(downtimeRequest:DowntimeRequest):HttpResponse = downtimeRequest.downtimeAction match {
    case GODOWN => {     
      logger info "Down request"
      //are we coordinators?
      if (distributedHost.isThisHost && distributedHost.isCoordinator){
	DistributedDowntimeStatus.canGoDownResponse
      }else{
	val extraHeader = ("Downtime","request")
	val response = HttpResponse.fromURL(coordinator.buildURL(""),Some(Seq(extraHeader)))
	
	println("****************************************RESPONSE****************************************")
	println(response)
	println("je")
	println(response.headerList)
	println("****************************************RESPONSE****************************************")
	response
      }      
    }
  }
}

object DistributedRequestHandler {
  def apply(distributedHost:Option[DistributedHost],coordinatorHost:DistributedHost):DistributedHttpRequest => HttpResponse = distributedHost match{
    case Some(dHost) => new DistributedRequestHandler(dHost,coordinatorHost)
    case noDHost => _ => DefaultErrorResponse(E423)
  }
}
