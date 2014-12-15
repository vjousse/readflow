package readflow.user

import readflow.app.Env

import scala.concurrent.Future
import scala.util.{Success, Failure}

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import reactivemongo.core.commands.LastError

import play.api.libs.concurrent.Execution.Implicits._

final class UserApi(
  userColl : BSONCollection) {

  import User.userBSONHandler

  def find(id: BSONObjectID): Future[Option[User]] =
    userColl.find(BSONDocument("_id" -> id)).one[User]


  def insert(user: User): Future[LastError] =
    userColl.insert(user)

  def getOrInsertUser(accessToken: String): Future[User] = {

    val accountInfo = Env.dropbox.dropboxApi.getAccountInfoForToken(accessToken)
    val query = BSONDocument("dropboxUserId" -> accountInfo.userId)
    val future: Future[Option[User]] = userColl.find(query).one

    future.flatMap { f =>
      f match {
        //If we found the user in the DB, return it
        case Some(user) => Future.successful(user)
        //If not, create it
        case None => {
          val user = User(BSONObjectID.generate, accessToken, accountInfo.userId)
          insert(user) map { lastError => user }
        }
      }
    }

  }

  def updateCursorForToken(cursor: String, token: String): Future[LastError] = {
    val selector = BSONDocument("accessToken" -> token)

    val modifier = BSONDocument(
      "$set" -> BSONDocument("cursor" -> cursor)
    )

    // get a future update
    userColl.update(selector, modifier)
  }

}

