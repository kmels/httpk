package kmels.uvg.httpk.service.http.distributed

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import kmels.uvg.httpk.util.typeAliases._
import kmels.uvg.httpk.service.http.{HttpStatus,HttpResponse}
import com.weiglewilczek.slf4s.Logging

case class DistributedHttpResponse(val status:HttpStatus, val html: () => HTML) extends HttpResponse
