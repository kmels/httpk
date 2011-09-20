package kmels.uvg.httpk.service

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.util.typeAliases.Port
import http.{HttpRequest,HttpResponse}
case class VirtualHost(val name:String, val listeningPorts: Option[List[Port]], val documentRoot:String, val handlers:Seq[Handler], val options:Seq[HttpkOption]) extends Cache{
   
  private val handlersPartition:(Seq[RegexHandler],Seq[ErrorHandler]) = (
    handlers collect {case r:RegexHandler => r},
    handlers collect {case e:ErrorHandler => e}  
  )

  val regexHandlers:Seq[RegexHandler] = handlersPartition._1
  val errorHandlers:Seq[ErrorHandler] = handlersPartition._2
}
