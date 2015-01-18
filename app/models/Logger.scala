package readflow
import play.api.{ Logger => DefaultPlayLogger }

trait Logger {
  def info(msg:String)
  def warn(msg:String)
  def error(msg:String)
  def debug(msg:String)
}

object PlayLogger extends Logger {

  def info(msg: String) = DefaultPlayLogger.info(msg)
  def warn(msg: String) = DefaultPlayLogger.warn(msg)
  def error(msg: String) = DefaultPlayLogger.error(msg)
  def debug(msg: String) = DefaultPlayLogger.debug(msg)

}
