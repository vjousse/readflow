import org.specs2.mutable._

import readflow.user.User
import readflow.user.UserApi

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.DB

import play.api.test._
import play.api.test.Helpers._

class UserApiSpec extends Specification {

  "Application" should {

    "do something" in new WithApplication{
      val config = readflow.app.PlayApp.loadConfig

      val storagePath = config.getString("dropbox.storagePath")
      val filesPrefix = config.getString("dropbox.filesPrefix")
      val htmlPrefix = config.getString("dropbox.htmlPrefix")

      lazy val db = {
        import play.api.Play.current
        ReactiveMongoPlugin.db
      }

      val user = User(
        BSONObjectID.generate,
        "accessToken",
        1,
        "Test user",
        "FR")

      lazy val userApi = new UserApi(
        db[BSONCollection]("user"),
        storagePath,
        filesPrefix,
        htmlPrefix)

      userApi.basePathForUser(user) must beEqualTo("/home/vjousse/usr/src/scala/readflow/test/data/dropbox/1/")
    }
  }

}
