package readflow.test

import org.specs2.mutable._

import readflow.user.User

import play.api.test._
import play.api.test.Helpers._

class UserApiSpec extends Specification {

  "UserApi paths" should {

    val projectDir = System.getProperty("user.dir")

    "convert .md paths to .html paths" in new ReadflowApplication {

      userApi.basePathForUser(user) must beEqualTo(projectDir + "/test/data/dropbox/1/")
      userApi.htmlPathForFilePath(projectDir + "/test/data/dropbox/1/files/test.md", user) must beEqualTo(projectDir + "/test/data/dropbox/1/html/test.html")
      userApi.htmlPathForFilePath(projectDir + "/test/data/dropbox/1/files/directory/test.md", user) must beEqualTo(projectDir + "/test/data/dropbox/1/html/directory/test.html")
    }

    "give path relative to the user base dir" in new ReadflowApplication {

      userApi.relativePathForUser(user, projectDir + "/test/data/dropbox/1/html/directory/test.html") must beSome("html/directory/test.html")

      userApi.relativePathForUser(user, "/blabla/test/data/dropbox/1/html/directory/test.html") must beNone

    }
  }

}
