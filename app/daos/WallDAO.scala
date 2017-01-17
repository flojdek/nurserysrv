package daos

import models.Post
import models.Wall
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import play.api.libs.json._

trait WallDAO {
  /** 
   * Return wall owned by user with `email` in which all posts have `_id` which
   * is smaller than `lastPostID` if `lastPostID` is not `None`.  If
   * `lastPostID` is `None` return wall with all posts starting from the most
   * recent post. Limit number of posts returned to `postsLimit`. 
   */
  def find(email: String, postsLimit: Int, lastPostID: Option[BSONObjectID] = None): Future[Option[Wall]]
}
