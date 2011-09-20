package kmels.uvg.httpk.service.http

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.util.typeConversions._
import kmels.uvg.httpk.util.IO._
import kmels.uvg.httpk.util.Settings
import HttpErrorCodes._
//import kmels.uvg.httpk.service.ErrorHandler
import com.weiglewilczek.slf4s.Logging

trait HttpResponse{
  import collection.mutable.ListBuffer

  //val contentType:String
  val status:HttpStatus
  val contentType:String = "text/html"
  val html: () => HTML

  val headerList:ListBuffer[String] = ListBuffer(
    "Server: Httpk (Ubuntu/Debian)",
    "Date: "+new java.util.Date().toString,
    "Content-Type: "+contentType,
    "Content-Length: "+html().size
  )

  def headerList_(header:String):HttpResponse = {
    headerList += header
    this
  }

  val headers: () => String = () => {
    "HTTP/1.0 "+status.toString+"\n"+ headerList.mkString("\n")+"\n\n\n"
  }

  def  mkString =  headers() + html()  
}

case class SimpleHttpResponse(val status:HttpStatus, val html:() => HTML,extraHeaders:Option[Seq[(String,String)]] = None) extends HttpResponse

case class ErrorHttpResponse(val status:HttpErrorStatus, val html:() => HTML) extends HttpResponse

case class BasicAuthResponse(val status:HttpErrorStatus, val html:() => HTML) extends HttpResponse{
  headerList += "WWW-Authenticate: Basic realm=\"Auth\""
}

object HttpResponse extends Logging{
  import collection.mutable.ArrayBuffer

  def fromFile(pathToFile:String,responseStatus:Option[HttpStatus] = None):HttpResponse = 
    try {
      val html = if (isDirectory(pathToFile))	
	listDirectory(pathToFile)
      else 
	readFile(pathToFile)
      
      val defaultStatusResponse = HttpSuccessStatus.S200
      new SimpleHttpResponse(responseStatus.getOrElse(defaultStatusResponse),() => html)
    } catch{      
      case HttpError(errorStatus) => ErrorHttpResponse(errorStatus,() => buildHtmlError(errorStatus))
    }

  def fromBytes(bytes:ArrayBuffer[Byte]):HttpResponse = {
    val html:HTML = bytes.toArray
    new SimpleHttpResponse(HttpSuccessStatus.S200,() => html)
  } 

  def fromURL(url:String,extraHeaders:Option[Seq[(String,String)]] = None):HttpResponse = {
    logger info "getting response from URL: "+url
    
    try {      
      val urlConnection = new java.net.URL(url).openConnection     
      val inputStream = urlConnection.getInputStream

      //add extra request headers
      extraHeaders match{
	case Some(headersSequence) => headersSequence.foreach(header => urlConnection.addRequestProperty(header._1,header._2))
	case _ =>{}
      }

      val html:String = inputStreamToString(inputStream)
      val responseHeaders:java.util.Map[String,java.util.List[String]] = urlConnection.getHeaderFields
      val statusCodeString = responseHeaders.get(null).get(0)
      val statusCodeRegex = "HTTP/... (\\d+) (\\w+)".r
      val statusCodeRegex2 = "HTTP/... (\\d+)/(\\w+)".r
      
      val statusResponse:(Int,String) = statusCodeString match{
	case statusCodeRegex(code,desc) => (code.toInt,desc)
	case statusCodeRegex2(code,desc) => (code.toInt,desc)
	case _ => (200,"OK..")
      }
      val status:HttpStatus = HttpStatus(statusResponse._1,statusResponse._2)
      val response = new SimpleHttpResponse(status,() => html)    
      //add headers
      import collection.JavaConversions._
      responseHeaders.keySet.foreach(key => {
	response.headerList += key+": "+responseHeaders.get(key).mkString(",")
      })

      response
    } catch {
      //error from getting URL
      case e:Exception => {
	logger error "Bad gateway to distributed host, reason: "+e.getMessage
	println("****************************************BEGIN BAD GATEWAY****************************************")

	e.printStackTrace
	println("****************************************END BAD GATEWAY****************************************")
	HttpError(HttpErrorCodes.E502)
      }
    }        
  }
}

object DefaultErrorResponse{
  def apply(errorCode:HttpErrorStatus):HttpResponse = HttpError(errorCode)
}

