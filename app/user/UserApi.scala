package readflow.user

import scala.concurrent.Future

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


}

