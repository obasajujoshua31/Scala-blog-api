package controllers

import javax.inject.Inject
import models.UserInfo
import play.api.mvc.Results._
import play.api.mvc._
import play.api.libs.json._
import utils._
import play.api.libs.functional.syntax._


import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


class AuthWrapper @Inject() (cc: ControllerComponents, jwt: JWT, bodyParser: BodyParsers.Default) (implicit ec: ExecutionContext)  extends ActionBuilder[UserRequest, AnyContent] {
  override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    val jwtToken = request.headers.get("token").getOrElse("")
    if (jwtToken == "") {
      Future.successful(Unauthorized("No token provided error"))
    } else {
        jwt.decodePayload(jwtToken.toString) match {
          case Failure(error) => Future.successful(Unauthorized("Invalid token"))
          case Success(payload) => {

            Json.parse(s"""$payload""").validate[UserInfo] match {
              case JsSuccess(userInfo: UserInfo, _) => {
                block(UserRequest(userInfo, request))
              }
              case JsError(e) => {
                Future.successful(Unauthorized("Invalid token"))
              }
            }
          }
        }
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  implicit val userInfoReads: Reads[UserInfo] = (
    (JsPath \ "userID").read[String] and
      (JsPath \ "nbf").read[String]
    )(UserInfo.apply _)
}
