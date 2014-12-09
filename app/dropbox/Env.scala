package readflow.dropbox

import akka.actor._
import com.typesafe.config.Config

import com.dropbox.core.{DbxAppInfo, DbxAuthFinish, DbxEntry, DbxWebAuthNoRedirect}
import com.dropbox.core.DbxEntry.WithChildren

final class Env(
    config: Config,
    system: ActorSystem,
    appPath: String) {


  // Get values from the config file. Don't handle the Option
  // as we want the application to crash if they are not
  // provided
  val applicationName = config.getString("dropbox.app.name")
  val version = config.getString("dropbox.app.version")
  val redirectUri = config.getString("dropbox.app.redirecturi")
  val appKey = config.getString("dropbox.app.key")
  val appSecret = config.getString("dropbox.app.secret")
  val oauthTokenUri = config.getString("dropbox.oauthtokenuri")

  val appInfo = new DbxAppInfo(appKey, appSecret);

  lazy val dropboxApi = new DropboxApi(
    applicationName,
    version,
    appKey,
    appSecret,
    redirectUri,
    oauthTokenUri)

}

object Env {

  lazy val current = new Env(
    config = readflow.app.PlayApp.loadConfig,
    system = readflow.app.PlayApp.system,
    appPath = readflow.app.PlayApp withApp (_.path.getCanonicalPath))

}
