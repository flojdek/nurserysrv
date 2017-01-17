package daos

import javax.inject._
import models.Post
import models.Post._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import utils.DAOUtil

class PostDAOImpl @Inject() (
  val reactiveMongoApi: ReactiveMongoApi)
  extends PostDAO {

  /**
   * Scala's default execution context.
   */
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * The 'user' collection handle.
   */
  val postCollection = reactiveMongoApi.db.collection[JSONCollection]("post")

  def save(post: Post): Future[Post] = {
    postCollection.insert(Json.toJson(post).as[JsObject])
    Future.successful(post)
  }

  def remove(postID: BSONObjectID): Future[BSONObjectID] = {
    postCollection.remove(Json.obj("_id" -> postID))
    Future.successful(postID)
  }
}
