package controllers

import java.util.Date

import dal._
import javax.inject._
import models.{Article, ArticleForm}
import play.api.libs.json._
import play.api.mvc._
import utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class ArticleController @Inject()(cc: ControllerComponents, articleDAL: ArticleDAL, jwt: JWT, password: Password, authWrapper: AuthWrapper, userDAL: UserDAL, uuid: Uuid) extends AbstractController(cc) {
  implicit val articleWrites: Writes[Article] = (o: Article) => {
    Json.obj(
      "id" -> o.id,
      "content" -> o.content,
      "title" -> o.title,
      "created_at" -> o.created_at,
      "author_id" -> o.author_id,
      "no_of_likes"-> o.no_of_likes,
      "no_of_comments" -> o.no_of_comments
    )
  }

    def createArticle: Action[AnyContent] = authWrapper.async { implicit request =>

      try {
        ArticleForm.form.bindFromRequest.fold (
          formErrors => {
            formErrors.errors.foreach(println)
            Future.successful(BadRequest("Bad data"))
          },
          success = data => {
            val userId = request.userInfo.userID
            val article = Article(uuid.generate, data.content, data.title, new Date().toString, userId)

            userDAL.findUserByID(userId) .flatMap {
              user => {
                articleDAL.createArticle(article) map {
                  res => Created(Json.toJson(article))
                }
              }
            }
          }
        )
      } catch {
        case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
      }
    }

    def getAllArticles : Action[AnyContent] = Action.async { implicit request => {
      try {
        articleDAL.getAllArticles map {
          articles => Ok(Json.toJson(articles))
        }
      } catch {
        case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
      }
    }
    }

    def getArticleById(id: String): Action[AnyContent] = Action.async { implicit request => {
      try {
              uuid.isValid(id)
              articleDAL.getArticleById(id.toString) map {
                article => if (article.isEmpty) {
                  NotFound("Could not find article")
                } else {
                  Ok(Json.toJson(article))
                }
              }
      } catch {
        case ex: UUIDValidationException => Future.successful(BadRequest(s"$id is not valid"))
        case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
      }
    }
    }

  def getArticlesByLoggedInUser: Action[AnyContent] = authWrapper.async { implicit request => {

      try {
        val userId = request.userInfo.userID
        articleDAL.getArticlesByUserId(userId) map {
          articles => if( articles.isEmpty) {
            NotFound("no articles found for user")
          } else {
            Ok(Json.toJson(articles))
          }
        }
      } catch {
        case ex: Exception => Future.successful(InternalServerError("Unknown error occurred"))
      }
    }
    }

  def updateArticle(id: String): Action[AnyContent] = authWrapper.async { implicit request => {
    try {
      val userId = request.userInfo.userID

      ArticleForm.form.bindFromRequest.fold(
        formErrors => {
          formErrors.errors.foreach(println)
          Future.successful(BadRequest("some credentials are missing"))
        },
        data => {
          uuid.isValid(id)
          articleDAL.getArticleById(id) flatMap {
            article => {
              if (article.isEmpty) {
               Future.successful(NotFound("Article is not found"))
              } else {
                if (article.get.author_id != userId) {
                  Future.successful(Forbidden("no permission"))
                } else {
                  articleDAL.updateArticle(Article(id, data.content, data.title, "", "")) map {
                    res => Ok(Json.toJson(Article(id, data.content, data.title, article.get.created_at, article.get.author_id)))
                  }
                }
              }
            }
          }
        }
      )
    } catch {
      case exec: UUIDValidationException => Future.successful(BadRequest(s"$id is not valid"))
      case ex: Exception => Future.successful(InternalServerError("unknown error while updating article"))
    }
  }
  }

  def deleteArticle(id: String): Action[AnyContent] = authWrapper.async { implicit request => {
    try {
      val userId = request.userInfo.userID

          uuid.isValid(id)
          articleDAL.getArticleById(id) flatMap {
            article => {
              if (article.isEmpty) {
                Future.successful(NotFound("Article is not found"))
              } else {
                if (article.get.author_id != userId) {
                  Future.successful(Forbidden("no permission"))
                } else {
                  articleDAL.deleteArticle(id) map {
                    res => NoContent
                  }
                }
              }
            }
          }
    } catch {
      case exec: UUIDValidationException => Future.successful(BadRequest(s"$id is not valid"))
      case ex: Exception => Future.successful(InternalServerError("unknown error while updating article"))
    }
  }
  }

  }
