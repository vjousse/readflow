package readflow.dropbox

import readflow.user.User
import java.security.SecureRandom
import com.dropbox.core.util.StringUtil

import dropbox4s.core.CoreApi
import dropbox4s.core.model.DropboxPath
import com.dropbox.core.{ DbxAuthFinish, DbxEntry }
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
  deltaUri: String) extends CoreApi {

  val applicationName = appName
  val version = appVersion

  val csrf = generateCsrf()

  val infos = DropboxInfos(csrf, appKey, appSecret, redirectUri)

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


  def syncFilesForToken(token: String): HashMap[String, Option[Metadata]]= {

    val files = getDeltasForToken(token).foldLeft(HashMap[String, Option[Metadata]]()) { (acc, delta) =>
      delta.entries.foldLeft(acc) { (acc2, entry) =>
        acc2 + ((entry._1, entry._2))
      }

    }

    files
  }

  def syncFiles() = {
    println("syncing files")
  }

  def getDeltasForToken(
    token: String,
    cursor: Option[String] = None): List[Delta] = {

    val response = Http(deltaUri)
      .method("POST")
      .headers("Authorization" -> s"Bearer $token")
      .asString

    parseResponse(response) match {
      case Left(err) => Nil
      case Right(body) => {

        val deltaJson = Json.parse(body).as[JsonDelta]
        val delta = deltaJson.toDelta{
          for(entry <- deltaJson.entries)
            yield (entry(0).as[String], entry(1).asOpt[Metadata])}

        if(delta.hasMore) {
          List(delta) ++ getDeltasForToken(token, Some(delta.cursor))
        } else {
          List(delta)
        }
      }
    }

  }

}
