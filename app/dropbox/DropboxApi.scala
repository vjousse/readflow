package readflow.dropbox

import java.security.SecureRandom
import com.dropbox.core.util.StringUtil

import dropbox4s.core.CoreApi
import dropbox4s.core.model.DropboxPath
import com.dropbox.core.{ DbxAuthFinish, DbxEntry }
import com.dropbox.core.DbxEntry.WithChildren

import scala.language.postfixOps
import scala.collection.JavaConverters._

import scalaj.http.Http

import play.api.libs.json._

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
  oauthTokenUri:String) extends CoreApi {

  val applicationName = appName
  val version = appVersion

  val csrf = generateCsrf()

  val infos = DropboxInfos(csrf, appKey, appSecret, redirectUri)

  def listDirectory(path: String, accessToken: String): List[DbxEntry] = {

      // Dirty hack, but dropbox4s requires the full DbxAuthFinish
      // object, even if it's only using the token
      implicit val auth: DbxAuthFinish = new DbxAuthFinish(accessToken, "", "")
      val appPath = DropboxPath(path)
      (appPath children).children.asScala.toList
  }

  def getAccessToken(code: String): Either[String, String] = {

    val response = Http(oauthTokenUri)
      .postForm(Seq("code" -> code, "grant_type" -> "authorization_code", "redirect_uri" -> infos.redirectUri))
      .auth(infos.appKey, infos.appSecret)
      .asString

      if(response.code == 200) {
        val json: JsValue = Json.parse(response.body)
        val access_token = (json \ "access_token").asOpt[String]
        access_token map { t => Right(t) } getOrElse Left("'access_token' not found in JSON respons")
      } else {
        Left("Bad response code from dropbox API: " + response.code)
      }
  }

  def generateCsrf(): String = {
    var r: SecureRandom = new SecureRandom();
    var csrfRaw: Array[Byte] = new Array[Byte](16);
    r.nextBytes(csrfRaw);

    StringUtil.urlSafeBase64Encode(csrfRaw);
  }


}
