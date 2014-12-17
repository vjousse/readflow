package readflow.dropbox

import readflow.user.User
import readflow.app.{ Env => AppEnv }
import java.security.SecureRandom
import java.io.File
import com.dropbox.core.util.StringUtil

import dropbox4s.core.CoreApi
import dropbox4s.core.model.DropboxPath
import com.dropbox.core.{ DbxAccountInfo, DbxAuthFinish, DbxEntry }
import com.dropbox.core.DbxEntry.WithChildren

import scala.language.postfixOps
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.HashMap

import scalaj.http.{ Http, HttpResponse }

import play.api.libs.json._
import play.api.libs.functional.syntax._

import readflow.dropbox.Reads._

import org.apache.commons.io.FileUtils

case class DropboxInfos(
  csrf: String,
  appKey: String,
  appSecret: String,
  redirectUri: String)

final class DropboxApi(
  appName: String,
  appVersion: String,
  appKey: String,
  appSecret: String,
  redirectUri: String,
  oauthTokenUri:String,
  deltaUri: String,
  storagePath: String) extends CoreApi {

  val applicationName = appName
  val version = appVersion

  val csrf = generateCsrf()

  val infos = DropboxInfos(csrf, appKey, appSecret, redirectUri)

  def getLocalUserDir(user: User) = storagePath + File.separator + user.dropboxUserId.toString + File.separator

  def listDirectory(path: String, accessToken: String): Future[List[DbxEntry]] = Future {

      // Dirty hack, but dropbox4s requires the full DbxAuthFinish
      // object, even if it's only using the token
      implicit val auth: DbxAuthFinish = new DbxAuthFinish(accessToken, "", "")
      val appPath = DropboxPath(path)
      (appPath children).children.asScala.toList
  }

  def parseResponse(response: HttpResponse[String]): Either[String, String] =
      if(response.code == 200) Right(response.body)
      else Left("Bad response code from dropbox API: " + response.code + "\n" + response.body)

  def getAccessToken(code: String): Either[String, String] = {

    val response = Http(oauthTokenUri)
      .postForm(Seq("code" -> code, "grant_type" -> "authorization_code", "redirect_uri" -> infos.redirectUri))
      .auth(infos.appKey, infos.appSecret)
      .asString

    parseResponse(response).right.map { body =>
        (Json.parse(body) \ "access_token").as[String]
    }
  }

  def generateCsrf(): String = {
    var r: SecureRandom = new SecureRandom();
    var csrfRaw: Array[Byte] = new Array[Byte](16);
    r.nextBytes(csrfRaw);

    StringUtil.urlSafeBase64Encode(csrfRaw);
  }


  def syncFilesForUser(user: User) = {

    val deltas = getDeltasForUser(user)

    deltas.map { delta =>
      if(delta.reset) resetUserDir(user)
      delta.entries.map { case (path, metadata) => syncFileMetadataForUser(user, path, metadata) }
      AppEnv.current.userApi.updateCursorForUser(delta.cursor, user)
    }

  }

  def syncFileMetadataForUser(user: User, path: String, metadata: Option[Metadata]) = {
    val localFile = new File(getLocalUserDir(user) + path)
    metadata match {
      case Some(metadata)                                         =>
        if (metadata.isDir) FileUtils.forceMkdir(localFile)
        else downloadFile(path, localFile.getAbsolutePath(), user.accessToken)
      case None if localFile.exists() && localFile.isDirectory()  => {
        //println("Deleting directory " + localFile)
        FileUtils.deleteDirectory(localFile)
      }
      case None if localFile.exists() && !localFile.isDirectory() => {
        //println("Deleting file " + localFile)
        localFile.delete()
      }
      case _                                                      => println(s"Doing nothing, local file $localFile doesn't exist")
    }
  }


  def resetUserDir(user: User) =
    FileUtils.deleteQuietly(new File(getLocalUserDir(user)))

  def downloadFile(remotePath: String, localPath: String, accessToken: String) = {
    // Dirty hack, but dropbox4s requires the full DbxAuthFinish
    // object, even if it's only using the token
    implicit val auth: DbxAuthFinish = new DbxAuthFinish(accessToken, "", "")
    val dropboxPath = DropboxPath(remotePath)
    dropboxPath downloadTo localPath
  }

  def syncFiles() = {
    println("syncing files")
    val fuUsers: Future[List[User]] = AppEnv.current.userApi.findAll()
    fuUsers.map { users =>
      users.map( user => syncFilesForUser(user) )
    }
  }

  def getAccountInfoForToken(token: String): DbxAccountInfo= {
    client(token).getAccountInfo()
  }

  def getDeltasForUser(user: User): List[Delta] = {
    val cursor = user.cursor.map(c => s"?cursor=$c").getOrElse("")
    val response = Http(deltaUri + cursor)
      .method("POST")
      .headers("Authorization" -> s"Bearer ${user.accessToken}")
      .asString

    parseResponse(response) match {
      case Left(err) => {
        println("Error when getting deltas: " + response)
        Nil
      }
      case Right(body) => {
        val deltaJson = Json.parse(body).as[JsonDelta]
        val delta = deltaJson.toDelta{
          for(entry <- deltaJson.entries)
            yield (entry(0).as[String], entry(1).asOpt[Metadata])}

        if(delta.hasMore) {
          List(delta) ++ getDeltasForUser(user.copy(cursor = Some(delta.cursor)))
        } else {
          List(delta)
        }
      }
    }

  }

}
