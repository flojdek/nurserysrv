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
import daos.UserDAO
import daos.WallDAO
import forms.{ SignInForm, SignUpForm }
import java.awt.Color
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.inject._
import models.Gallery
import models.Photo
import models.Post
import models.User
import models.Wall._
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

@Singleton class GalleryController @Inject() (
  val env: Environment[User, CookieAuthenticator],
  val socialProviderRegistry: SocialProviderRegistry,
  val messagesApi: MessagesApi,
  val system: ActorSystem,
  val reactiveMongoApi: ReactiveMongoApi,
  val userDAO: UserDAO,
  val wallDAO: WallDAO,
  val postDAO: PostDAO)
  extends Silhouette[User, CookieAuthenticator] {

  val AWSs3BucketName = current.configuration.getString("aws.s3.bucketName").get
  val AWSaccessKeyID = current.configuration.getString("aws.accessKeyID").get
  val AWSsecretAccessKey = current.configuration.getString("aws.secretAccessKey").get
  val AWScredentials = new BasicAWSCredentials(AWSaccessKeyID, AWSsecretAccessKey)
  val AWSs3Client = new AmazonS3Client(AWScredentials)

  val addPostForm = Form(
    tuple(
      "post-title" -> nonEmptyText,
      "post-text" -> nonEmptyText,
      "post-emails" -> nonEmptyText,
      "photo-urls" -> text
    )
  )

  def getAddGallery = SecuredAction.async { implicit request =>
    userDAO.findAll().map { users =>
      Ok(views.html.user_addgallery(request.identity, users))
    }
  }

  def postAddGallery = SecuredAction.async { implicit request =>
    println("postAddGallery: request.body=" + request.body)
    addPostForm.bindFromRequest.fold(
      formWithErrors => {
        println(s"error=${formWithErrors}")
        Future.successful(Ok(""))
      },
      data => {
        val title = data._1
        val text = data._2
        val emails = data._3.split(",")
        val photos = if (data._4.isEmpty) List() else Json.parse(data._4).validate[List[Photo]] match {
          case s: JsSuccess[List[Photo]] => s.get
          case e: JsError => List()
        }
        val gallery = Gallery(None, photos)
        userDAO.find(emails).map { users =>
          postDAO.save(Post(None, users.map(_.id.get), Some(title), new org.joda.time.DateTime(), text, gallery))
          Redirect(routes.WallController.getWall())
        }
      }
    )
  }

  def postAddGalleryPhoto = Action.async(parse.multipartFormData) { request =>
    Future.successful(Ok(
      request.body.file("file").map { file =>
        println("DashboardController: postUploadImage: file=" + file.filename)
        val scaled = Image.fromStream(new FileInputStream(file.ref.file)).fit(800, 600, Color.BLACK)
        //var thumbnail = scaled.fit(320, 240, Color.BLACK)

        val uniqueFileName = FileStoreUtil.genUniqueFileName(file.filename)

        val nowDate = java.time.LocalDate.now
        val AWSscaledFilePath = nowDate.toString + "/scaled_" + uniqueFileName + ".png"
        //val AWSthumbnailFilePath = nowDate.toString + "/thumbnail_" + uniqueFileName + ".png"

        val AWSobjectMetadata = new ObjectMetadata()
        val scaledResult = AWSs3Client.putObject(new PutObjectRequest(AWSs3BucketName,
                                                                      AWSscaledFilePath,
                                                                      scaled.stream(PngWriter.NoCompression),
                                                                      AWSobjectMetadata))

        //val thumbnailResult = AWSs3Client.putObject(new PutObjectRequest(AWSs3BucketName,
        //                                                                 AWSthumbnailFilePath,
        //                                                                 thumbnail.stream(PngWriter.MaxCompression),
        //                                                                 AWSobjectMetadata))

        // memory info
        val mb = 1024*1024
        val runtime = Runtime.getRuntime
        println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
        println("** Free Memory:  " + runtime.freeMemory / mb)
        println("** Total Memory: " + runtime.totalMemory / mb)
        println("** Max Memory:   " + runtime.maxMemory / mb)


        Json.toJson(Photo(None,
                          "",
                          AWSs3Client.getResourceUrl(AWSs3BucketName,
                                                     AWSscaledFilePath)))
        //Json.toJson(Photo(None,
        //                  AWSs3Client.getResourceUrl(AWSs3BucketName,
        //                                             AWSthumbnailFilePath),
        //                  AWSs3Client.getResourceUrl(AWSs3BucketName,
        //                                             AWSscaledFilePath)))
      }.getOrElse {
        Json.obj("status" -> "error")
      }
    ))
  }
}
