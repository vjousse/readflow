package readflow.test

import org.specs2.mutable._

import readflow.user.User

import reactivemongo.bson.{ BSONObjectID, BSONDocument }

import play.api.test._
import play.api.test.Helpers._

class UserApiSpec extends Specification {

  "UserApi paths" should {

    "should convert .md paths to .html paths" in new ReadflowApplication {

      val user = User(
        BSONObjectID.generate,
        "accessToken",
        1,
        "Test user",
        "FR")

      val projectDir = System.getProperty("user.dir")
      userApi.basePathForUser(user) must beEqualTo(projectDir + "/test/data/dropbox/1/")
      userApi.htmlPathForFilePath(projectDir + "/test/data/dropbox/1/files/test.md", user) must beEqualTo(projectDir + "/test/data/dropbox/1/html/test.html")
      userApi.htmlPathForFilePath(projectDir + "/test/data/dropbox/1/files/directory/test.md", user) must beEqualTo(projectDir + "/test/data/dropbox/1/html/directory/test.html")
    }
  }

}
