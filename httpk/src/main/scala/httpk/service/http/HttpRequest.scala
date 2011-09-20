package kmels.uvg.httpk.service.http

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.service.DistributedHost

object HttpMethod extends Enumeration{
  type HttpMethod = Value
  val GET,POST = Value
}

import HttpMethod._

class HttpRequest(
  val method:HttpMethod,
  val hostName:String, 
  val port:Port,
  val relativeURL:String,
  val auth:Option[(String,String)] = None){

    override def toString = "Request: method="+method+", host="+hostName+", port="+port+" URI="+relativeURL
    
    import kmels.uvg.httpk.service.http.DistributedHttpRequest
    def toDistributedRequest(distributedHosts:Seq[DistributedHost]):Option[DistributedHttpRequest] = DistributedHttpRequest.unapply(this,distributedHosts)    											      
  }

