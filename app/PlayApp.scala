package readflow.app


import com.typesafe.config.Config
import play.api.{ Play, Application, Mode }

object PlayApp {

  def loadConfig: Config = withApp(_.configuration.underlying)

  def loadConfig(prefix: String): Config = loadConfig getConfig prefix

  def withApp[A](op: Application => A): A =
    Play.maybeApplication map op getOrElse sys.error("Play application is not started!")

  def system = withApp { implicit app =>
    play.api.libs.concurrent.Akka.system
  }

  private def enableScheduler = !(loadConfig getBoolean "app.scheduler.disabled")

  def scheduler = system.scheduler

  lazy val isDev = isMode(_.Dev)
  lazy val isTest = isMode(_.Test)
  lazy val isProd = isMode(_.Prod) && !loadConfig.getBoolean("forcedev")
  def isServer = !isTest

  def isMode(f: Mode.type => Mode.Mode) = withApp { _.mode == f(Mode) }
}
