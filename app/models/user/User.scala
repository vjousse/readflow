package readflow.user

import reactivemongo.bson.BSONObjectID

case class User(
  _id: BSONObjectID,
  accessToken: String,
  lastDropboxCursor: Option[String] = None)

object User {
  import reactivemongo.bson.Macros
  implicit val userBSONHandler = Macros.handler[User]

  def createWithToken(accessToken: String): User =
    User(BSONObjectID.generate, accessToken)

}
