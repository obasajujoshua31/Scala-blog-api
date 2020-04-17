package controllers

import java.util.UUID

import dal._
import javax.inject._
import models._
import play.api.libs.json._
import play.api.mvc._
import utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

case class AuthResponse(token: String, value: String)

case class UserRequest[A](userInfo: UserInfo, request: Request[A]) extends  WrappedRequest(request)


@Singleton
class AuthController @Inject()(cc: ControllerComponents, userDAL: UserDAL, jwt: JWT, password: Password) extends AbstractController(cc) {
  implicit val tokenWrites = new Writes[AuthResponse] {
    override def writes(o: AuthResponse): JsValue = {
      Json.obj(
        "token" -> o.token,
        "value" -> o.value
      )
    }
  }

//  implicit val userInfoReads = new Reads[UserInfo] {

    //    override def reads(json: JsValue): JsResult[UserInfo] = {
    //    var res =  (json \ "userID").get
    //
    //    }
    //  }


    def home(): Action[AnyContent] = Action { implicit request =>
      Ok("Welcome Home")
    }

    def register(): Action[AnyContent] = Action.async { implicit request =>
      try {
        UserForm.form.bindFromRequest.fold(
          error => {
            error.errors.foreach(println)
            Future.successful(BadRequest("Error"))
          },
          success = data => {
            var newUser: User = User("", "", "", "")
            val createdUser = userDAL.findUserByEmail(data.email) map {
              value => {
                if (value.isEmpty) {
                  newUser = User(UUID.randomUUID().toString, data.name, data.email, data.password)
                  newUser
                } else {
                  throw new Exception("Email is not available")
                }
              }
            }

            val response = createdUser flatMap {
              value =>
                userDAL.createUser(value) map {
                  res => res
                }
            } recover {
              ex => throw new Exception(ex)
            }

            response map {
              res => Created(Json.toJson(AuthResponse(jwt.createToken(newUser.id), res.toString)))
            }
          } recover {
            ex => BadRequest(ex.getMessage)
          }
        )
      } catch {
        case ex: Exception => Future.successful(InternalServerError(ex.getMessage))
      }
    }

    def login(): Action[AnyContent] = Action.async { implicit request => {
      try {
        UserLoginFormat.form.bindFromRequest.fold(
          hasErrors => {
            hasErrors.errors.foreach(println)
            Future.successful(BadRequest("Bad credentials"))
          },
          data => {
            userDAL.findUserByEmail(data.email) map {
              user =>
                if (user.isEmpty) {
                  BadRequest("Invalid login credentials")
                } else {
                  password.isMatchPassword(data.password, user.get.password) match {
                    case Success(value) => {
                      if (value) {
                        Ok(Json.toJson(AuthResponse(jwt.createToken(user.get.id), "1")))
                      } else {
                        BadRequest("Invalid login credentials")
                      }
                    }
                    case Failure(exception) => BadRequest(exception.getMessage)
                  }
                }
            }
          }
        )
      } catch {
        case ex: Exception => Future.successful(InternalServerError("Unknown error occured"))
      }
    }
  }
}
