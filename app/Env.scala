package readflow.app

import readflow.user.UserApi
import readflow.ebook.EbookApi
import readflow.dropbox.DropboxApi

import akka.actor._
import com.typesafe.config.Config

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

final class Env(
    config: Config,
    system: ActorSystem,
    appPath: String,
    scheduler: Scheduler,
    dropboxApi: DropboxApi) {

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

  lazy val ebookApi = new EbookApi(userApi, PlayLogger)

  lazy val sync = new Sync(userApi, dropboxApi, ebookApi, PlayLogger)

  // Sync Dropbox files every minute

  PlayLogger.info("Starting scheduler")
  scheduler.schedule(0.microsecond, 1.minute)(sync.syncFiles)
}

object Env {

  def dropbox = readflow.dropbox.Env.current

  lazy val current = new Env(
    config = readflow.app.PlayApp.loadConfig,
    system = readflow.app.PlayApp.system,
    appPath = readflow.app.PlayApp withApp (_.path.getCanonicalPath),
    scheduler = readflow.app.PlayApp.scheduler,
    dropboxApi = dropbox.dropboxApi
  )

}
