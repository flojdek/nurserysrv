package daos

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject._
import models.User
import play.api.libs.json._
import play.api.Logger
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import scala.concurrent.Future
import utils.DAOUtil

class UserDAOImpl @Inject() (
  val reactiveMongoApi: ReactiveMongoApi)
  extends UserDAO {

  /**
   * Scala's default execution context.
   */
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * The 'user' collection handle.
   */
  val userCollection = reactiveMongoApi.db.collection[JSONCollection]("user")

  def find[R](query: JsObject, f: User => R, limit: Option[Int]): Future[Seq[R]] = {
    val cursor = userCollection.find(query).cursor[JsObject]()
    (limit.map { cursor.collect[List](_) } getOrElse cursor.collect[List]())
    .map(_.foldLeft(List(): List[R]) { (acc, userJson) =>
      userJson.validate[User] match {
        case s: JsSuccess[User] => f(s.get) :: acc
        case e: JsError => acc
      }
    })
  }

  def findAll(): Future[Seq[User]] = {
    find(Json.obj(), identity)
  }

  def find(emails: Seq[String]): Future[Seq[User]] = {
    find(Json.obj("email" -> Json.obj("$in" -> emails)), identity)
  }

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    find(Json.obj(
      "loginInfo" -> Json.obj(
        "providerID" -> loginInfo.providerID,
        "providerKey" -> loginInfo.providerKey
      )
    ), identity, Some(1)).map { _.headOption }
  }

  def save(user: User): Future[User] = {
    val userWithID = user.copy(id = Some(user.id.getOrElse(BSONObjectID.generate)))
    userCollection.insert(Json.toJson(userWithID).as[JsObject])
    Future.successful(userWithID)
  }
}
