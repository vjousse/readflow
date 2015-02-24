package readflow.test

import play.api.test._
import play.api.test.Helpers._

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection

import readflow.user.UserApi
import readflow.ebook.EbookApi

class ReadflowApplication extends WithApplication {

  val config = readflow.app.PlayApp.loadConfig

  val storagePath = config.getString("dropbox.storagePath")
  val filesPrefix = config.getString("dropbox.filesPrefix")
  val htmlPrefix = config.getString("dropbox.htmlPrefix")

  val db = {
    import play.api.Play.current
    ReactiveMongoPlugin.db
  }

  val userApi = new UserApi(
    db[BSONCollection]("user"),
    storagePath,
    filesPrefix,
    htmlPrefix)

  val ebookApi = new EbookApi(
    userApi,
    readflow.app.PlayLogger)

}
