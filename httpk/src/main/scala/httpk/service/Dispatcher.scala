package kmels.uvg.httpk.service

/**
 * Receives HTTP requests and dispatches it to a thread
 * @author Carlos López-Camey
 * @since 2.0
 */

import scala.actors.{Actor,IScheduler}

object Dispatcher{
  def dispatch(body: => Unit)(implicit scheduler:IScheduler):Actor = {
    val dispatcher = new Actor {
      def act() = body
      override final val scheduler: IScheduler = scheduler
    }
    dispatcher.start()
    dispatcher
  }
}

