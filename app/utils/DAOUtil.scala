package utils

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.Future

object DAOUtil {
  def find[R](userCollection: JSONCollection, loginInfo: LoginInfo, f: User => R): Future[Option[R]] = {
    println("DAOUtil: find: ..., loginInfo=" + loginInfo + ", ...")
    val query = Json.obj(
      "loginInfo" -> Json.obj(
        "providerID" -> loginInfo.providerID,
        "providerKey" -> loginInfo.providerKey
      )
    )
    userCollection
    .find(query)
    .cursor[JsObject]()
    .headOption
    .map(_ match {
      case Some(userJson) => {
        userJson.validate[User] match {
          case s: JsSuccess[User] => Some(f(s.get))
          case e: JsError => None
        }
      }
      case None => None
    })
  }
}
