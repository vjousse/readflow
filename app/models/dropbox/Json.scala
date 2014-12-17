package readflow.dropbox

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class JsonDelta(
  hasMore: Boolean,
  reset: Boolean,
  cursor: String,
  entries: List[List[JsValue]]) {

  def toDelta(e: List[(String, Option[Metadata])]): Delta =
    Delta(hasMore, reset, cursor, e)

}

case class Delta(
  hasMore: Boolean,
  reset: Boolean,
  cursor: String,
  entries: List[(String, Option[Metadata])])


case class Metadata(
  bytes: Int,
  rev: String,
  revision: Int,
  icon: String,
  path: String,
  isDir: Boolean,
  thumbExists: Boolean,
  root: String,
  modified: String,
  size: String)

object Reads {

  implicit val jsonDeltaReads: Reads[JsonDelta] = (
    (JsPath \ "has_more").read[Boolean] and
    (JsPath \ "reset").read[Boolean] and
    (JsPath \ "cursor").read[String] and
    (JsPath \ "entries").read[List[List[JsValue]]]
  )(JsonDelta.apply _)

  implicit val metadataReads: Reads[Metadata] = (
    (JsPath \ "bytes").read[Int] and
    (JsPath \ "rev").read[String] and
    (JsPath \ "revision").read[Int] and
    (JsPath \ "icon").read[String] and
    (JsPath \ "path").read[String] and
    (JsPath \ "is_dir").read[Boolean] and
    (JsPath \ "thumb_exists").read[Boolean] and
    (JsPath \ "root").read[String] and
    (JsPath \ "modified").read[String] and
    (JsPath \ "size").read[String]
  )(Metadata.apply _)

}
