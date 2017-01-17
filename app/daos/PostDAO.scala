package daos

import models.Post
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future

trait PostDAO {
  def save(post: Post): Future[Post]

  def remove(postID: BSONObjectID): Future[BSONObjectID]
}
