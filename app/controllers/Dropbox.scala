package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import com.dropbox.core.{DbxAppInfo, DbxAuthFinish, DbxWebAuth}

import scalaj.http.Http

object Dropbox extends Controller {

  val app: Application = Play.unsafeApplication
  val conf: Configuration = app.configuration

  val applicationName = "Readflow"
  val version = "0.0.1"
  val redirectUri = "http://localhost:9000/dropbox/auth-finish"

  // Get values from the config file. Don't handle the Option
  // as we want the application to crash if they are not
  // provided
  val appKey = conf.getString("dropbox.app.key").get
  val appSecret = conf.getString("dropbox.app.secret").get

  def index = Action { request =>
    val csrf = models.Dropbox.generateCsrf

    // Store the csrf value in the session
    Ok(views.html.dropbox.index(appKey, redirectUri, csrf)).withSession(request.session + ("csrf" -> csrf))
  }

  def authFinish(code: String, state: String) = Action { request =>
    // Check if the csrf sent by the callback is the same than the one
    // we previously stored in the session
    request.session.get("csrf").map { csrf =>

      if(csrf == state) {
        val response = Http("https://api.dropbox.com/1/oauth2/token")
          .postForm(Seq("code" -> code, "grant_type" -> "authorization_code", "redirect_uri" -> redirectUri))
          .auth(appKey, appSecret)
          .asString

        if(response.code == 200) {
          val json: JsValue = Json.parse(response.body)
          val access_token: String = (json \ "access_token").as[String]
          Ok(views.html.dropbox.authFinish()).withSession(request.session + ("access_token" -> access_token))
        } else {
          InternalServerError("Error when finishing the oAuth process.")
        }

      } else
        Unauthorized("Csrf values doesn't match.")
    }.getOrElse {
      Unauthorized("Bad csrf value.")
    }
  }

}

