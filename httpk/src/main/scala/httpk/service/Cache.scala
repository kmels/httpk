package kmels.uvg.httpk.service

/**
 *
 * @author Carlos Lopez-Camey
 * @since 2.0
 */

import collection.mutable.{ArrayBuffer,HashMap}

trait Cache{
  self => VirtualHost

  val cacheArray:ArrayBuffer[(String,ArrayBuffer[Byte])] = ArrayBuffer.fill(5)("",new ArrayBuffer) //(pathToFile,bytes)

  private object fileNameIndexes extends HashMap[String,Int] // fileName -> index in cache
  private var lastUpdatedIndex:Int = -1

  val nextIndex: () => Int = () => {
    if (lastUpdatedIndex<4)
      lastUpdatedIndex +=1 
    else
      lastUpdatedIndex = 0

    lastUpdatedIndex
  }

  def removeOldestEntry:Int = {
    val oldestEntryIndex:Int = nextIndex()
    fileNameIndexes.remove(cacheArray(oldestEntryIndex)._1)
    oldestEntryIndex
  }
  

  /**
   * Caches a new file
   */
  def encache(pathToFile:String,bytes:ArrayBuffer[Byte]):Unit = {
    val nextIndexToUpdate = removeOldestEntry
    fileNameIndexes.put(pathToFile,nextIndexToUpdate)
    cacheArray(fileNameIndexes(pathToFile)) = (pathToFile,bytes)
  }

  /**
   * @return an optional byte array, representing a cached file
   */
  def cache(pathToFile:String):Option[ArrayBuffer[Byte]] = fileNameIndexes.get(pathToFile) match {
    case Some(index) => Some(cacheArray(index)._2)
    case _ => None
  }  
}
