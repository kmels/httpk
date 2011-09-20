package kmels.uvg.httpk.service

/**
 * A user handler
 * 
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.service.http.HttpResponse

trait HttpkOption{
  val flag:Boolean  
}

case class IndexesOption(val flag:Boolean) extends HttpkOption

case class FollowSymLinks(val flag:Boolean) extends HttpkOption

