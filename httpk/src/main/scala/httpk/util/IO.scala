package kmels.uvg.httpk.util

/**
 * Provides utilities to network classes
 * @author Carlos López-Camey
 * @since 2.0
 */

import java.io.{InputStream,FileInputStream,File}
import collection.mutable.ArrayBuffer
import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.service.http.HttpErrorCodes._
import kmels.uvg.httpk.service.http.{HttpMethod,HttpRequest,DistributedHttpRequest}
import HttpMethod._
import java.lang.NumberFormatException

package object IO{

  import java.io.{InputStream,FileInputStream,File}
  import collection.mutable.ArrayBuffer
  
  implicit def arrayBufferToArray(barray:ArrayBuffer[Byte]):Array[Byte] = barray.toArray

  implicit def byteArrayToString(array:Array[Byte]):String = array.map(_.toChar).mkString

  implicit def intToByte(x:Int):Byte = x.toByte

  implicit def inputStreamToString(is:InputStream):String = {
    val in = is
    var toRead = in.available
    
    //block!
    while (toRead==0)
      toRead = in.available

    println("TO READ: "+toRead)
    val bytes = new ArrayBuffer[Byte](toRead)
    while (toRead > 0){
      bytes += in.read
      toRead -= 1
    }
    byteArrayToString(bytes)
  }
  
  def readFile(pathToFile:String) = {
    inputStreamToString(new FileInputStream(new File(pathToFile)))
  }
  
  def listDirectory(pathToFile:String):HTML = {
    if (isDirectory(pathToFile)){
      val directory:File = new File(pathToFile)
      getDirectoryHTML(directory.getName,directory.listFiles)
    }else ""
  }

  def isDirectory(pathToFile:String):Boolean = new File(pathToFile).isDirectory
 
  def parseRequest(inputWithNewLines:String):Option[HttpRequest] = {
    println("****************************************BEGIN REQUEST****************************************")
    println(inputWithNewLines)    
    
    import sun.misc.BASE64Decoder

    val input = inputWithNewLines.replaceAll("\n","").replaceAll("\r"," ").trim
    val requestREGEX = """(?i)(GET|POST) /(.+)? HTTP/1.\d.*Host: (.+?):(\d+)(.*)""".r

    val authREGEX = """.*Authorization: Basic (.*)""".r
    input match{
      case requestREGEX(method,requestedURL,hostName,port,leftRequest) => {
	val httpMethod:HttpMethod = method match{
	  case "GET" => GET
	  case "POST" => POST
	}

	val auth:Option[(String,String)] = leftRequest match{
	  case authREGEX(hash) => {
	    val authInfo:Array[Char] = new BASE64Decoder().decodeBuffer(hash).map(_.toChar)
	    val user = authInfo.takeWhile(_!=58).mkString
	    val pw = authInfo.dropWhile(_!=58).tail.mkString
	    Some(user,pw)
	  }
	  case _ => None
	}

	val relativeURL:String = if (requestedURL==null) "" else {
	  val url = requestedURL.replace("%20"," ")
	  //strip ending slash for caching purposes
	  if (url.charAt(url.size-1)=='/')
	    url.take(url.size-1)
	  else
	    url
	}
	
	try {
	  println("SOME")
	  println("****************************************END REQUEST****************************************")
	  Some(new HttpRequest(httpMethod,hostName,port.toInt,relativeURL,auth))	  
	} catch{
	  case e:NumberFormatException => {
	    println("SOME")
	    println("****************************************END REQUEST****************************************")
	    None
	  }
	}
      }
      case _ => {
	println("REGEX FAIL")
	println("****************************************END REQUEST****************************************")
	None
      }
    }
  }

  /**
   * @return an html response based on the error code
   */
  def buildHtmlError(errorCode:HttpErrorStatus):HTML = {
      docTypeHeader + """<html>
      <head>
      <title>Error """+errorCode+"""</title>
      <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=ISO-8859-1">
      </head>
      <BODY><H1>Error """+errorCode+"""</H1></BODY>

      <address>Httpk</address>
      </html>"""
  }

  def getDirectoryHTML(directoryName:String,files:Array[File]):HTML = docTypeHeader + """
  <html>
  <head>
  <title> Index of """+directoryName+"""/</title>
  </head>
  <body><h1>Index of """+directoryName+"""/</h1>
  <ul>"""+files.map(f => "<li><a href=\""+f.getName+"/\">"+ (if (f.isDirectory) f.getName+"/" else f.getName)+"</a></li>").mkString("\n")+"""
  </ul></body>
  </html>
  """

  def docTypeHeader:HTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd\">"
}
