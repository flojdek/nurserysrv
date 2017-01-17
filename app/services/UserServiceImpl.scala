package pl.malekrasnale.services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import java.util.UUID
import javax.inject.Inject
import daos.UserDAO
import models.User
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userDAO.find(loginInfo)
  }

  def save(user: User) = {
    userDAO.save(user)
  }

  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update 'user' with 'profile'.
        userDAO.save(user.copy(email = profile.email.get))
      case None => // Insert a new user.
        userDAO.save(User(None, profile.loginInfo, None, "normal", profile.email.get, None))
    }
  }
}
