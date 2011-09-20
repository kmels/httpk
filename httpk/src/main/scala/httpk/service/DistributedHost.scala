package kmels.uvg.httpk.service

/**
 *
 * @author Carlos Lopez-Camey
 * @since 3.0
 */

import kmels.uvg.httpk.util.typeAliases.Port
import http.{HttpRequest,HttpResponse}

case class DistributedHost(val name: String, 
			   val ip: String, 
			   val port:Int, 
			   val priority:Int) extends Cache{  
//  val requestHandler: HttpRequest => HttpResponse = new DistributedRequestHandler(this) 

  def isThisHost = name match {
    case "kmels" => true
    case _ => false
  }

  def isCoordinator = priority match{
    case 1 => true
    case _ => false
  }

  def buildURL(uri:String,auth:Option[(String,String)] = None):String = auth match {
    case Some(auth) => "http://"+auth._1+":"+auth._2+"@"+this.ip+":"+port+"/"+uri
    case _ => "http://"+this.ip+":"+port+"/"+uri
  }
}
