package daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import javax.inject._
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.collection.mutable
import scala.concurrent.Future
import utils.DAOUtil

class PasswordInfoDAO @Inject() (
  val reactiveMongoApi: ReactiveMongoApi)
  extends DelegableAuthInfoDAO[PasswordInfo] {

  /**
   * Scala's default execution context.
   */
  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * The 'user' collection handle.
   */
  val userCollection = reactiveMongoApi.db.collection[JSONCollection]("user")

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {

    println("PasswordInfoDAO: find: loginInfo=" + loginInfo)

    DAOUtil.find(userCollection, loginInfo, user => user.authInfo.get)
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

    println("PasswordInfoDAO: add: loginInfo=" + loginInfo + ", authInfo=" + authInfo)

    val obj = Json.obj(
      "loginInfo" -> Json.obj(
        "providerID" -> loginInfo.providerID,
        "providerKey" -> loginInfo.providerKey
      ),
      "authInfo" -> Json.obj(
        "hasher" -> authInfo.hasher,
        "password" -> authInfo.password,
        "salt" -> authInfo.salt
      )
    )
    userCollection.insert(obj)
    Future.successful(authInfo)
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

    println("PasswordInfoDAO: update: loginInfo=" + loginInfo + ", authInfo=" + authInfo)

    val query = Json.obj(
      "loginInfo" -> Json.obj(
        "providerID" -> loginInfo.providerID,
        "providerKey" -> loginInfo.providerKey
      )
    )
    val update = Json.obj(
      "$set" -> Json.obj(
        "authInfo" -> Json.obj(
          "hasher" -> authInfo.hasher,
          "password" -> authInfo.password,
          "salt" -> authInfo.salt
        )
      )
    )
    userCollection.update(query, update)
    Future.successful(authInfo)
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {

    println("PasswordInfoDAO: save: loginInfo=" + loginInfo + ", authInfo=" + authInfo)

    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  def remove(loginInfo: LoginInfo): Future[Unit] = {

    println("PasswordInfoDAO: remove: loginInfo=" + loginInfo)

    val query = Json.obj(
      "loginInfo" -> Json.obj(
        "providerID" -> loginInfo.providerID,
        "providerKey" -> loginInfo.providerKey
      )
    )
    val update = Json.obj(
      "$unset" -> Json.obj(
        "loginInfo" -> "",
        "authInfo" -> ""
      )
    )
    userCollection.update(query, update)
    Future.successful(())
  }
}
