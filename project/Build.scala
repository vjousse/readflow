import sbt._
import Keys._
import play.Play.autoImport._

object ApplicationBuild extends Build {

  val appName         = "readflow-scala"
  val appVersion      = "0.0.1"

  val appDependencies = Seq(
    ws,
    cache,
    "com.github.Shinsuke-Abe" %% "dropbox4s" % "0.2.0",
    "org.scalaj" %% "scalaj-http" % "1.0.1",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    libraryDependencies ++= appDependencies,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq(
      "bintray" at "http://dl.bintray.com/shinsuke-abe/maven"
    )
  )

}
