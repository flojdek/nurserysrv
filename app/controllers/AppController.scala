package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import authorization.AdminPermission
import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms.{ SignInForm, SignUpForm }
import javax.inject._
import models.User
import daos.UserDAO
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
import views.html._

@Singleton class AppController @Inject() (
  val env: Environment[User, CookieAuthenticator],
  val socialProviderRegistry: SocialProviderRegistry,
  val messagesApi: MessagesApi,
  val system: ActorSystem,
  val reactiveMongoApi: ReactiveMongoApi,
  val userDAO: UserDAO)
  extends Silhouette[User, CookieAuthenticator] {

  def getSignIn = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.WallController.getWall())
      case None => Ok(views.html.signin(SignInForm.form, socialProviderRegistry))
    }
  }

  def getAllUsers = SecuredAction.async { implicit request =>
    userDAO.findAll().map { users =>
      Ok(Json.toJson(users.map { user =>
        Json.obj("email" -> user.email)
      }))
    }
  }

  def getAllUsersPage = SecuredAction(AdminPermission).async { implicit request =>
    userDAO.findAll().map { users =>
      Ok(views.html.dashboard_allusers(request.identity, users))
    }
  }

  def getSignUp = SecuredAction(AdminPermission) { implicit request =>
    Ok(views.html.signup(Some(request.identity), SignUpForm.form))
  }

  def getSignOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.AppController.getIndex)
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, result)
  }

  def getIndex = UserAwareAction { implicit request =>
    Ok(views.html.about(request.identity))
  }

  def getAbout = UserAwareAction { implicit request =>
    Ok(views.html.about(request.identity))
  }

  def getGallery = UserAwareAction { implicit request =>
    Ok(views.html.gallery(request.identity))
  }

  def getContact = UserAwareAction { implicit request =>
    Ok(views.html.contact(request.identity))
  }

  def getPricing = UserAwareAction { implicit request =>
    Ok(views.html.pricing(request.identity))
  }

  def getChildKit = UserAwareAction { implicit request =>
    Ok(views.html.childkit(request.identity))
  }

  def getDocuments = UserAwareAction { implicit request =>
    Ok(views.html.documents(request.identity))
  }

  def getSchedule = UserAwareAction { implicit request =>
    Ok(views.html.schedule(request.identity))
  }
}
