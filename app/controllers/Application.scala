package controllers

import play.api._
import play.api.mvc._

import com.dropbox.core.{DbxAppInfo, DbxAuthFinish, DbxWebAuth}

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }

  def logout() = Action { implicit request =>
    Redirect(routes.Dropbox.index()).withNewSession
  }

}
