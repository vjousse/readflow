package controllers

import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import readflow.app._

import readflow.user.User

private[controllers] trait ReadflowController
    extends Controller {

  def addUserToCache(user: User): User = {
    //Store user in the cache by it's dropbox id
    Cache.set(user.dropboxUserId.toString, user)
    user
  }

  def getUserFromCache(id: String): Option[User] =
    Cache.getAs[User](id)

}
