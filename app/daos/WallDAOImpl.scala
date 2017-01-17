package daos

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject._
import models.Post
import models.Wall
import play.api.libs.json._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import pl.malekrasnale.utils.MongoDBUtil._
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future

class WallDAOImpl @Inject() (
  val reactiveMongoApi: ReactiveMongoApi)
  extends WallDAO {

  /**
   * Scala's default execution context.
   */
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * The 'user' collection handle.
   */
  val userCollection = reactiveMongoApi.db.collection[JSONCollection]("user")

  /**
   * The 'post' collection handle.
   */
  val postCollection = reactiveMongoApi.db.collection[JSONCollection]("post")

  def find(email: String, postsLimit: Int, lastPostID: Option[BSONObjectID]): Future[Option[Wall]] = {
    userCollection
    .find(Json.obj("email" -> email))
    .cursor[JsObject]()
    .headOption
    .flatMap(_ match {
      case Some(userJson) => {
        (userJson \ "_id").validate[BSONObjectID] match {
          case s: JsSuccess[BSONObjectID] => {
            val query = lastPostID.map { id =>
              Json.obj(
                "owners" -> s.get,
                "_id" -> Json.obj("$lt" -> id)
              )
            } getOrElse Json.obj("owners" -> s.get)
            postCollection
            .find(query)
            .sort(Json.obj("_id" -> -1))
            .cursor[JsObject]()
            .collect[List](postsLimit)
            .map(posts => {
              Some(Wall(None, posts.foldRight(List(): List[Post])((x, acc) => {
                x.validate[Post] match {
                  case s: JsSuccess[Post] => s.get :: acc
                  case e: JsError => acc
                }
              })))
            })
          }
          case e: JsError => Future.successful(None)
        }
      }
      case None => Future.successful(None)
    })
  }
}
