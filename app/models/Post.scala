package models

import models.Gallery._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson._
import pl.malekrasnale.utils.MongoDBUtil._

case class Post(id: Option[BSONObjectID],
                owners: Seq[BSONObjectID],
                title: Option[String],
                created: DateTime,
                text: String,
                gallery: Gallery)

object Post {
  implicit val postWrites: Writes[Post] = (
    (__ \ "_id").writeNullable[BSONObjectID] and
    (__ \ "owners").write[Seq[BSONObjectID]] and
    (__ \ "title").writeNullable[String] and
    (__ \ "created").write[DateTime] and
    (__ \ "text").write[String] and
    (__ \ "gallery").write[Gallery]
  )(unlift(Post.unapply))

  implicit val postReads: Reads[Post] = (
    (__ \ "_id").readNullable[BSONObjectID] and
    (__ \ "owners").read[Seq[BSONObjectID]] and
    (__ \ "title").readNullable[String] and
    (__ \ "created").read[DateTime] and
    (__ \ "text").read[String] and
    (__ \ "gallery").read[Gallery]
  )(Post.apply _)
}
