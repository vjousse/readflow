package readflow.app
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import readflow.user.{ User, UserApi }
import readflow.dropbox.{ DropboxApi }

final class Sync(
  userApi: UserApi,
  dropboxApi: DropboxApi,
  logger: Logger) {

  def syncFiles() = {
    logger.debug("syncing files")
    val fuUsers: Future[List[User]] = userApi.findAll()
    fuUsers.map { users =>
      users.flatMap( user => dropboxApi.syncFilesForUser(user) )
    }
  }

}
