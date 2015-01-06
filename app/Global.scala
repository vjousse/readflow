package readflow.app

import play.api.{ Application, GlobalSettings }

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    readflow.app.Env.current
    readflow.app.Env.dropbox
  }

}
