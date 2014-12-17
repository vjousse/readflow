package readflow.user
import readflow.app.Env

import reactivemongo.bson.BSONObjectID

case class User(
  _id: BSONObjectID,
  accessToken: String,
  dropboxUserId: Long,
  cursor: Option[String] = None)

object User {
  import reactivemongo.bson.Macros
  implicit val userBSONHandler = Macros.handler[User]

}
