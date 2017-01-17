package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Wall(title: Option[String], posts: Seq[Post])

object Wall {
  val emptyWall = Wall(None, List())

  implicit val wallWrites: Writes[Wall] = (
    (__ \ "title").writeNullable[String] and
    (__ \ "posts").write[Seq[Post]]
  )(unlift(Wall.unapply))

  implicit val wallReads: Reads[Wall] = (
    (__ \ "title").readNullable[String] and
    (__ \ "posts").read[Seq[Post]]
  )(Wall.apply _)
}
