package controllers

import play.api.mvc._

import readflow.app.Env
import readflow.user.User
import readflow.dropbox.{ DropboxInfos, DropboxApi }

import com.dropbox.core.DbxEntry
import scala.util.{Success, Failure}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Dropbox extends ReadflowController {

  def index = Action { request =>
    val infos : DropboxInfos = Env.dropbox.dropboxApi.infos
    // Store the csrf value in the session
    Ok(views.html.dropbox.index(infos.appKey, infos.redirectUri, infos.csrf)).withSession(request.session + ("csrf" -> infos.csrf))
  }

  def authFinish(code: String, state: String) = Action.async { request =>
    // Check if the csrf sent by the callback is the same than the one
    // we previously stored in the session
    request.session.get("csrf").map { csrf =>

      if(csrf == state) {
          Env.dropbox.dropboxApi.getAccessToken(code) match {
            case Left(err)    => Future.successful(InternalServerError("Error when finishing the oAuth process: " + err))
            case Right(token) => {
              Env.current.userApi.getOrInsertUser(token).map {
                user => Ok(views.html.dropbox.authFinish()).withSession(request.session + ("access_token" -> token))
              }
            }
          }
      } else
        Future.successful(Unauthorized("Csrf values doesn't match."))
    }.getOrElse {
      Future.successful(Unauthorized("Bad csrf value."))
    }
  }

  def listDirectory() = Action.async { request =>

    request.session.get("access_token").map { accessToken =>
      Env.dropbox.dropboxApi.syncFilesForToken(accessToken)
      Env.dropbox.dropboxApi.listDirectory("/", accessToken).map { children =>
        Ok(views.html.dropbox.listDirectory(children))
      }
    }.getOrElse {
      Future.successful(Unauthorized("No access_token available."))
    }

  }

}
