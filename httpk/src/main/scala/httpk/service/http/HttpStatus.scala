package kmels.uvg.httpk.service.http

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import java.lang.Throwable
import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.util.IO._
import java.io.FileNotFoundException

trait HttpStatus{
  val statusCode:Int
  val description:String
  override def toString = statusCode+" "+description
}

object HttpStatus {
  def apply(code:Int,desc:String) = new HttpStatus{
    val statusCode = code
    val description = desc
  }
}

package object HttpStatusSet{
  import HttpSuccessStatus._
  import HttpErrorCodes._
  
}

object HttpSuccessStatus extends Enumeration{
  case class HttpSuccessStatus(val statusCode:Int,val description:String) extends Value with HttpStatus{
    def id = statusCode    
  }
  
  val S200 = HttpSuccessStatus(200,"OK")  
  val S201 = HttpSuccessStatus(201,"Created")
}

object HttpErrorCodes extends Enumeration{
  case class HttpErrorStatus(val statusCode:Int,val description:String) extends Value with HttpStatus{
    def id = statusCode
  }

  val E404 = HttpErrorStatus(404,"Not Found")
  val E401 = HttpErrorStatus(401,"Authorization Required")
  val E403 = HttpErrorStatus(403,"Forbidden")  
  val E409 = HttpErrorStatus(409,"Conflict")
  val E422 = HttpErrorStatus(422,"No virtual host matches request")
  val E423 = HttpErrorStatus(423,"No distributed host matches request")  
  val E502 = HttpErrorStatus(502,"Bad Gateway")  
  
  def errors:List[HttpErrorStatus] = this.values.toList.flatMap{
    case e:HttpErrorStatus => List(e)
    case _ => Nil
  }
}

object HttpError{
  import HttpErrorCodes._

  def unapply(thrown:Throwable):Option[HttpErrorStatus] = thrown match{
    case e404:FileNotFoundException => Some(E404)
    case _ => None
  }

  def apply(errorStatus:HttpErrorStatus):HttpResponse = {
    new ErrorHttpResponse(errorStatus,() => buildHtmlError(errorStatus))
  }
}

