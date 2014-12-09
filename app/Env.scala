package readflow.app

import akka.actor._
import com.typesafe.config.Config

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB


final class Env(
    config: Config,
    system: ActorSystem,
    appPath: String) {

  lazy val db = {
    import play.api.Play.current
    ReactiveMongoPlugin.db
  }

}

object Env {

  lazy val current = new Env(
    config = readflow.app.PlayApp.loadConfig,
    system = readflow.app.PlayApp.system,
    appPath = readflow.app.PlayApp withApp (_.path.getCanonicalPath))

}
