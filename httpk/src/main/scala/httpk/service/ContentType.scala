package kmels.uvg.httpk.service

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

object ContentType {
  private val images = Map(
    "gif" -> "image/gif",
    "jpeg" -> "image/jpeg",
    "png" -> "image/png"
  )
  
  private val all = images

  def unapply(extension:String):Option[String] = all.get(extension)
}
