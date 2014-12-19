package readflow.user

import readflow.app.Env

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import reactivemongo.core.commands.LastError

import java.io.File

import play.api.libs.concurrent.Execution.Implicits._

final class UserApi(
  userColl : BSONCollection,
  storagePath: String) {

  import User.userBSONHandler

  def find(id: BSONObjectID): Future[Option[User]] =
    userColl.find(BSONDocument("_id" -> id)).one[User]

  def findByDropboxId(id: Long): Future[Option[User]] =
    userColl.find(BSONDocument("dropboxUserId" -> id)).one[User]

  def findAll(): Future[List[User]] =
    userColl.
    find(BSONDocument()).
    cursor[User].
    collect[List]()


  def insert(user: User): Future[LastError] =
    userColl.insert(user)

  def pathForUser(user: User): String =
    storagePath + File.separator + user.dropboxUserId + File.separator

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
          val user = User(
            BSONObjectID.generate,
            accessToken,
            accountInfo.userId,
            accountInfo.displayName,
            accountInfo.country)
          insert(user) map { lastError => user }
        }
      }
    }

  }

  def updateCursorForUser(cursor: String, user: User): Future[LastError] = {
    val selector = BSONDocument("accessToken" -> user.accessToken)

    val modifier = BSONDocument(
      "$set" -> BSONDocument("cursor" -> cursor)
    )

    // get a future update
    userColl.update(selector, modifier)
  }


}

