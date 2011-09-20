package kmels.uvg.httpk.service

/**
 * 
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import collection.mutable.ListMap
import http._
import com.weiglewilczek.slf4s.Logging

object DistributedHostStatus extends Logging{
  //in case we're coordinators
 /* val users:ListMap[String,String] = ListMap() //user -> password     
  
  def addUser(user:String,password:String):HttpResponse = {
    logger info "Trying to add user "+user+":"+password
    if (users.contains(user)){
      logger info "User already exists"
      HttpError(HttpErrorCodes.E409)
    }      
    else{
      logger info "Created user"
      //everything ok
      users += ((user,password))    
      SimpleHttpResponse(HttpSuccessStatus.S201,() => "")
    }
  }

  def editUser(user:String,password:String):HttpResponse = {
    logger info "Trying to edit user's password to "+user+":"+password
    if (users.contains(user)){
      //everything ok
      logger info "User password updated"
      users += ((user,password))    
      SimpleHttpResponse(HttpSuccessStatus.S201,() => "")    
    }else{
      logger info "User doesnt exist"
      HttpError(HttpErrorCodes.E404)
    }
  }
  
  def deleteUser(user:String):HttpResponse = {
    logger info "Trying to delete user "+user
    if (users.contains(user)){
      //everything ok
      logger info "Deleted successfuly"
      users.remove(user)
      SimpleHttpResponse(HttpSuccessStatus.S201,() => "")    
    }else{
      logger info "User doesn't exist"
      HttpError(HttpErrorCodes.E404)
    }
  }*/
}
 
