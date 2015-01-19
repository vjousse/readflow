package readflow.app
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import readflow.user.{ User, UserApi }
import readflow.dropbox.{ DropboxApi }
import readflow.ebook.{ EbookApi }

final class Sync(
  userApi: UserApi,
  dropboxApi: DropboxApi,
  ebookApi: EbookApi,
  logger: Logger) {

  def syncFiles() = {
    logger.debug("syncing files")
    val fuUsers: Future[List[User]] = userApi.findAll()
    fuUsers.map { users =>
      users.map { user =>
          dropboxApi.syncFilesForUser(user)
          ebookApi.createEbooksForUser(user)
      }
    }
  }

}
