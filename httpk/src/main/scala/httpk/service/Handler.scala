package kmels.uvg.httpk.service

/**
 * A user handler
 * 
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import http.HttpErrorCodes._
import http.{HttpResponse,BasicAuthResponse,HttpRequest,HttpErrorCodes}
import kmels.uvg.httpk.util.typeConversions._

abstract class Handler extends (() => HttpResponse){
  val matchesRequest: HttpRequest => Boolean
}

case class ErrorHandler(val errorStatus:HttpErrorStatus,val pathToFile:String) extends Handler{  
  def apply:HttpResponse = HttpResponse.fromFile(pathToFile,Some(errorStatus))
  
  val matchesRequest: HttpRequest => Boolean = request => false //error handlers are never requested by the client, we try find them before sending the response.

  override def toString = errorStatus+"=>"+pathToFile
}

trait RegexHandler extends Handler{ 
  import http.{HttpSuccessStatus,HttpStatus}
  import java.util.regex.{Pattern}

  val regex:String
  val file:String  

  val matchesRequest: HttpRequest => Boolean = request => Pattern.compile(regex).matcher(request.relativeURL).matches
}

case class MatchHandler(val regex:String,val file:String) extends RegexHandler{
  def apply:HttpResponse = HttpResponse.fromFile(file)
}
case class AuthHandler(val regex:String,val file:String) extends RegexHandler{
  //TODO: response an auth request
  def apply:HttpResponse = BasicAuthResponse(HttpErrorCodes.E401,() => "")

  def authenticates(request:HttpRequest):Boolean = request.auth match{
    case Some(authInfo) => {
      val user = authInfo._1
      val pw = authInfo._2
      user==pw
    }
    case _ => false
  }
}

