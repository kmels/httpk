package kmels.uvg.httpk.main

import kmels.uvg.httpk.network.Servlet

/**
 * 
 * @author Carlos Lopez
 * @version 1.0
 */

import kmels.uvg.httpk.util.Settings

object Main extends Application{
  override def main(args: Array[String]) = {
    //load config    
    //val pathToConfigFile = args.head
    val pathToConfigFile = System.getProperty("user.dir")+"/src/main/resources/config.xml"
    
    val settings = Settings(pathToConfigFile)
    
    settings match{
      case Some(settings) => {
	try {
	  val servlet = new Servlet(settings)
	  servlet.listen
	} catch{
	  case e => println(e)
	}
      } 
      case _ => {
	println("Could not parse config file")
      }
    }   
  }
}
