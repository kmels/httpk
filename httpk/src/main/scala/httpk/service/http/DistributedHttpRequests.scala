package kmels.uvg.httpk.service.http

/**
 *
 * @author Carlos Lopez-Camey
 * @since 3.0
 */

import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.util.typeAliases.Port
import kmels.uvg.httpk.service.DistributedHost
import kmels.uvg.httpk.service.http.HttpMethod._

object UserManagementActionType extends Enumeration {
  type UserManagementActionType = Value
  val CREATE,EDIT,DELETE = Value

  def apply(action:String):Value = action match {
    case "createuser" => CREATE
    case "edituser" => EDIT
    case "delete" => DELETE
  }
}

object DowntimeActionType extends Enumeration {
  type DowntimeActionType = Value
  val GODOWN,GETUP = Value

  def apply(action:String):Value = action match {
    case "down" => GODOWN
    case "getup" => GETUP
  }
}

import UserManagementActionType._
import DowntimeActionType._

object DistributedHttpRequest{
  val manageUserRegex = "(createuser|edituser|delete)/(.+?)/(.+?)".r  
  val downtimeRegex = "(down|getup)".r
  val validateUser = "(authenticate)".r

  def unapply(httpRequest:HttpRequest,distributedHosts:Seq[DistributedHost]):Option[DistributedHttpRequest] = {
   
    val distributedHostByAction:Option[DistributedHttpRequest] = httpRequest.relativeURL match{
      case manageUserRegex(action,username,password) => Some(
	UserManagementRequest(
	  UserManagementActionType(action),
	  username,
	  password,
	  httpRequest.hostName))
      case downtimeRegex(action) => Some(
	DowntimeRequest(
	  DowntimeActionType(action),
	  httpRequest.hostName
	))
      case validateUser(authenticate) => Some(
	UserValidationRequest(
	  httpRequest.auth,
	  httpRequest.hostName
	)
      )
      case _ => None
    }

    distributedHostByAction match{
      case Some(dHost) => distributedHostByAction
      case _ => {
	//it's not a distributed action.
	
	//distributed request?
	val distributedHostByName = distributedHosts.find(dhost => dhost.name==httpRequest.hostName && !dhost.isThisHost) 

	distributedHostByName match{
	  case None => None
	  case Some(distributedHost) => httpRequest.auth match{
	    case Some(auth) =>
	      Some(DistributedUserAuthRequest(auth._1,auth._2,distributedHost.name,httpRequest.relativeURL))
	    case _ => 
	      //normal distributed request e.g. http://node/uri

	      Some(DistributedFileRequest(distributedHost.name,httpRequest.relativeURL)) 	    
	  }
	}	  
      }
    }    
  }  
}

trait DistributedHttpRequest{
  val distributedHostName:String
}

case class UserValidationRequest(val auth:Option[(String,String)],val distributedHostName:String) extends DistributedHttpRequest 

case class DistributedUserAuthRequest(val username:String,
				    val password:String,
				    val distributedHostName:String,
				    val URI:String
				    ) extends DistributedHttpRequest{
}

case class DistributedFileRequest(val distributedHostName:String, 
				val uri:String) extends DistributedHttpRequest

case class DowntimeRequest(val downtimeAction:DowntimeActionType, val distributedHostName:String) extends DistributedHttpRequest

case class UserManagementRequest(
  val action:UserManagementActionType,
  val username:String, 
  val password:String, 
  val distributedHostName:String
  ) extends DistributedHttpRequest
