package kmels.uvg.httpk.util

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

object typeAliases{
  type HTML = String
  type Port = Int
  type Connection = java.net.ServerSocket
}

object typeConversions{
  import kmels.uvg.httpk.service.http._
  import UserManagementActionType._
  import HttpErrorCodes._
  import collection.mutable.ArrayBuffer
  import typeAliases._

  implicit def userManagementActionToString(actionType:UserManagementActionType):String = actionType match{
    case CREATE => "createuser"
    case EDIT => "edituser"
    case DELETE => "delete"
  }

  implicit def httpStatusCodeToInt(httpStatus:HttpStatus):Int = httpStatus.statusCode
  implicit def intToHttpHttpErrorStatus(code:Int):HttpErrorStatus = {
    HttpErrorCodes.errors.find(_.statusCode==code) match{
      case Some(errorCode) => errorCode
      case _ => HttpErrorStatus(code,"")
    }
  }
  def HTMLToBytes(html:HTML):ArrayBuffer[Byte] = {
    val size:Int = html.size
    val arrayB:ArrayBuffer[Byte] = new ArrayBuffer(size)
    html.foreach(char => arrayB += char.toByte)
    arrayB
  }
}
