package daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import scala.concurrent.Future
import play.api.libs.json._

trait UserDAO {

  def find[R](query: JsObject, f: User => R, limit: Option[Int] = None): Future[Seq[R]]

  def findAll(): Future[Seq[User]]

  def find(emails: Seq[String]): Future[Seq[User]]

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def save(user: User): Future[User]
}
