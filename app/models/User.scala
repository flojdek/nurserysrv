package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.api.util.PasswordInfo
import play.api.libs.functional.syntax._
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID
import pl.malekrasnale.utils.MongoDBUtil._

case class User(
  id: Option[BSONObjectID] = None,
  loginInfo: LoginInfo,
  authInfo: Option[PasswordInfo],
  permission: String,
  email: String,
  name: Option[String]) extends Identity

object User {
  implicit val passwordInfoWrites: Writes[PasswordInfo] = (
    (JsPath \ "hasher").write[String] and
    (JsPath \ "password").write[String] and
    (JsPath \ "salt").writeNullable[String]
  )(unlift(PasswordInfo.unapply))

  implicit val passwordInfoReads: Reads[PasswordInfo] = (
    (JsPath \ "hasher").read[String] and
    (JsPath \ "password").read[String] and
    (JsPath \ "salt").readNullable[String]
  )(PasswordInfo.apply _)

  implicit val userWrites: Writes[User] = (
    (JsPath \ "_id").writeNullable[BSONObjectID] and
    (JsPath \ "loginInfo").write[LoginInfo] and
    (JsPath \ "authInfo").writeNullable[PasswordInfo] and
    (JsPath \ "permission").write[String] and
    (JsPath \ "email").write[String] and
    (JsPath \ "name").writeNullable[String]
  )(unlift(User.unapply))

  implicit val userReads: Reads[User] = (
    (JsPath \ "_id").readNullable[BSONObjectID] and
    (JsPath \ "loginInfo").read[LoginInfo] and
    (JsPath \ "authInfo").readNullable[PasswordInfo] and
    (JsPath \ "permission").read[String] and
    (JsPath \ "email").read[String] and
    (JsPath \ "name").readNullable[String]
  )(User.apply _)

  def isAdmin(user: User): Boolean = {
    user.permission == "admin"
  }
}
