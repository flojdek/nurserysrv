package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Photo(title: Option[String],
                 thumbnailUrl: String,
                 photoUrl: String)

object Photo {
  implicit val photoWrites: Writes[Photo] = (
    (__ \ "title").writeNullable[String] and
    (__ \ "thumbnailUrl").write[String] and
    (__ \ "photoUrl").write[String]
  )(unlift(Photo.unapply))

  implicit val photoReads: Reads[Photo] = (
    (__ \ "title").readNullable[String] and
    (__ \ "thumbnailUrl").read[String] and
    (__ \ "photoUrl").read[String]
  )(Photo.apply _)
}
