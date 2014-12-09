package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import scalaj.http.Http

import readflow.app.Env
import readflow.dropbox.{ DropboxInfos, DropboxApi }

import com.dropbox.core.DbxEntry

object Dropbox extends ReadflowController {

  def index = Action { request =>
    val infos : DropboxInfos = Env.dropbox.dropboxApi.infos
    // Store the csrf value in the session
    Ok(views.html.dropbox.index(infos.appKey, infos.redirectUri, infos.csrf)).withSession(request.session + ("csrf" -> infos.csrf))
  }

  def authFinish(code: String, state: String) = Action { request =>
    // Check if the csrf sent by the callback is the same than the one
    // we previously stored in the session
    request.session.get("csrf").map { csrf =>

      if(csrf == state) {
        val infos : DropboxInfos = Env.dropbox.dropboxApi.infos

        val response = Http("https://api.dropbox.com/1/oauth2/token")
          .postForm(Seq("code" -> code, "grant_type" -> "authorization_code", "redirect_uri" -> infos.redirectUri))
          .auth(infos.appKey, infos.appSecret)
          .asString

        if(response.code == 200) {
          val json: JsValue = Json.parse(response.body)
          val access_token: String = (json \ "access_token").as[String]

          // Storing the access_token in the session, it's certainly not a good idea
          // but hey, it's a test case ;)
          Ok(views.html.dropbox.authFinish()).withSession(request.session + ("access_token" -> access_token))
        } else {
          InternalServerError("Error when finishing the oAuth process: " + response.body)
        }

      } else
        Unauthorized("Csrf values doesn't match.")
    }.getOrElse {
      Unauthorized("Bad csrf value.")
    }
  }

  def listDirectory() = Action { request =>

    request.session.get("access_token").map { accessToken =>

      val children: List[DbxEntry] = Env.dropbox.dropboxApi.listDirectory("/", accessToken)

      Ok(views.html.dropbox.listDirectory(children))
    }.getOrElse {
      Unauthorized("No access_token available.")
    }

  }

}

