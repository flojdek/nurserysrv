package models

import models.Photo._
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Gallery(title: Option[String],
                   photos: Seq[Photo])

object Gallery {
  implicit val galleryWrites: Writes[Gallery] = (
    (__ \ "title").writeNullable[String] and
    (__ \ "photos").write[Seq[Photo]]
  )(unlift(Gallery.unapply))

  implicit val galleryReads: Reads[Gallery] = (
    (__ \ "title").readNullable[String] and
    (__ \ "photos").read[Seq[Photo]]
  )(Gallery.apply _)
}
