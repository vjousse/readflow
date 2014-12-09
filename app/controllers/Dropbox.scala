package controllers

import play.api._
import play.api.mvc._
import play.api.Configuration

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
          Env.dropbox.dropboxApi.getAccessToken(code) match {
            case Left(err)    => InternalServerError("Error when finishing the oAuth process: " + err)
            case Right(token) => Ok(views.html.dropbox.authFinish()).withSession(request.session + ("access_token" -> token))
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
