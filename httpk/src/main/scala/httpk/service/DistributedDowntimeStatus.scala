package kmels.uvg.httpk.service

/**
 * 
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import collection.mutable.ListMap
import http._
import com.weiglewilczek.slf4s.Logging

object DistributedDowntimeStatus extends Logging{
  //in case we're coordinators
  private var anyoneDown = false
  private var areWeDown = false

  def isAnyoneDown = anyoneDown
  def isThisHostDown = areWeDown

  def canGoDownResponse:HttpResponse = 
    if (isAnyoneDown){
      logger info "Nobody is down, allowing"
      new SimpleHttpResponse(HttpSuccessStatus.S200,() => ""){
	headerList += "Downtime: allowed"
      }
    }else{
      logger info "Someone is already down"
      new SimpleHttpResponse(HttpSuccessStatus.S200,() => ""){
	headerList += "Downtime: rejected"
      }
    }  

  def goDown:Unit = { 
    logger info "Getting down"
    this.areWeDown = true
  }
  
  def getUp:Unit = { 
    logger info "Going up"
    this.areWeDown = false
  }
  
}
 
