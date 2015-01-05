package readflow.app

import readflow.user.UserApi
import readflow.ebook.EbookApi

import akka.actor._
import com.typesafe.config.Config

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection


final class Env(
    config: Config,
    system: ActorSystem,
    appPath: String,
    scheduler: Scheduler) {

  val storagePath = config.getString("dropbox.storagePath")
  val filesPrefix = config.getString("dropbox.filesPrefix")
  val htmlPrefix = config.getString("dropbox.htmlPrefix")

  lazy val db = {
    import play.api.Play.current
    ReactiveMongoPlugin.db
  }

  lazy val userApi = new UserApi(
    db[BSONCollection]("user"),
    storagePath,
    filesPrefix,
    htmlPrefix)

  lazy val ebookApi = new EbookApi(userApi)

}

object Env {

  lazy val current = new Env(
    config = readflow.app.PlayApp.loadConfig,
    system = readflow.app.PlayApp.system,
    appPath = readflow.app.PlayApp withApp (_.path.getCanonicalPath),
    scheduler = readflow.app.PlayApp.scheduler)

  def dropbox = readflow.dropbox.Env.current
}
