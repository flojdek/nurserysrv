package controllers

import akka.actor._
import authorization.AdminPermission
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ CannedAccessControlList, ObjectMetadata, PutObjectRequest }
import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.PngWriter
import daos.PostDAO
import daos.WallDAO
import forms.{ SignInForm, SignUpForm }
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.inject._
import models.User
import models.Wall
import models.Wall._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current
import play.modules.reactivemongo._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.duration._
import scala.concurrent.Future
import utils.FileStoreUtil
import views.html._
import reactivemongo.bson.BSONObjectID

@Singleton class WallController @Inject() (
  val env: Environment[User, CookieAuthenticator],
  val messagesApi: MessagesApi,
  val wallDAO: WallDAO,
  val postDAO: PostDAO)
  extends Silhouette[User, CookieAuthenticator] {

  implicit val dtFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")

  def getWallByEmail(email: String, lastPostID: Option[String]) = SecuredAction(AdminPermission).async { implicit request =>
    wallDAO.find(email, 10, lastPostID.map(BSONObjectID(_))).map { wall =>
      Ok(views.html.user_wall(request.identity, email, wall.getOrElse(Wall.emptyWall)))
    }
  }

  def getWallJsonByEmail(email: String, lastPostID: Option[String]) = SecuredAction(AdminPermission).async { implicit request =>
    wallDAO.find(email, 10, lastPostID.map(BSONObjectID(_))).map { wall =>
      val x = Json.toJson(wall.getOrElse(Wall.emptyWall))
      println(x)
      Ok(x)
    }
  }

  def getWall(lastPostID: Option[String]) = SecuredAction.async { implicit request =>
    val user = request.identity
    wallDAO.find(user.email, 10, lastPostID.map(BSONObjectID(_))).map { wall =>
      Ok(views.html.user_wall(user, user.email, wall.getOrElse(Wall.emptyWall)))
    }
  }

  def getWallJson(lastPostID: Option[String]) = SecuredAction.async { implicit request =>
    val user = request.identity
    wallDAO.find(user.email, 10, lastPostID.map(BSONObjectID(_))).map { wall =>
      val x = Json.toJson(wall.getOrElse(Wall.emptyWall))
      println(x)
      Ok(x)
    }
  }

  def removePost(postID: String) = SecuredAction { implicit request =>
    println(s"WallController: removePost: postID=${postID}")
    postDAO.remove(BSONObjectID(postID))  
    Ok("")
  }
}
