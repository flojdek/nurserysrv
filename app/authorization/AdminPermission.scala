package authorization

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import concurrent.Future
import models.User
import play.api.i18n.{ Messages, MessagesApi }
import play.api.mvc._

case object AdminPermission extends Authorization[User, CookieAuthenticator] {
  def isAuthorized[B](user: User, authenticator: CookieAuthenticator)
                     (implicit request: Request[B], messages: Messages) = {
    println("AdminPermission: isAuthroized: ..., user=" + user + ", ...")
    Future.successful(user.permission == "admin")
  }
}
