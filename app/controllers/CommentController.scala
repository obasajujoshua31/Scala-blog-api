package controllers

import dal._
import javax.inject._
import models.{Comment, CommentForm}
import play.api.libs.json._
import play.api.mvc._
import utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class CommentController @Inject()(cc: ControllerComponents, articleDAL: ArticleDAL, commentDAL: CommentDAL, jwt: JWT, authWrapper: AuthWrapper, userDAL: UserDAL, uuid: Uuid) extends AbstractController(cc) {
  implicit val articleWrites: Writes[Comment] = (o: Comment) => {
    Json.obj(
      "id" -> o.id,
      "content" -> o.content,
      "no_of_likes"-> o.no_of_likes,
      "article_id" -> o.article_id,
      "reviewer_id" -> o.reviewer_id
    )
  }

  def createComment(articleId: String): Action[AnyContent] = authWrapper.async { implicit request =>

    try {
      CommentForm.form.bindFromRequest.fold (
        formErrors => {
          formErrors.errors.foreach(println)
          Future.successful(BadRequest("Bad data"))
        },
        success = data => {
          val userId = request.userInfo.userID

          articleDAL.getArticleById(articleId) flatMap {
            article => {
              if (article.isEmpty) {
                Future.successful(BadRequest("article is not found"))
              } else {
                val comment = Comment(uuid.generate, data.content, userId, article.get.id)
                commentDAL.createComment(comment, article.get) map {
                 res => Ok(Json.toJson(comment))
                }
              }
            }
          }
        }
      )
    } catch {
      case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
    }
  }

  def getAllArticleComments(articleId: String) : Action[AnyContent] = Action.async { implicit request => {
    try {
      uuid.isValid(articleId)
      commentDAL.getAllCommentsOnArticleId(articleId) map {
        comments => {
          if (comments.isEmpty) {
            NotFound("no comments found for article")
          } else {
            Ok(Json.toJson(comments))
          }
        }
      }
    } catch {
      case ex: UUIDValidationException => Future.successful(BadRequest(s"$articleId is not valid"))
      case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
    }
  }
  }

  def getCommentsById(id: String): Action[AnyContent] = Action.async { implicit request => {
    try {
      uuid.isValid(id)
      commentDAL.getCommentById(id) map {
        comment => if (comment.isEmpty) {
          NotFound("Could not find comments")
        } else {
          Ok(Json.toJson(comment))
        }
      }
    } catch {
      case ex: UUIDValidationException => Future.successful(BadRequest(s"$id is not valid"))
      case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
    }
  }
  }


  def getCommentsByLoggedInUser: Action[AnyContent] = authWrapper.async { implicit request => {

    try {
      val userId = request.userInfo.userID
      commentDAL.getCommentsByUserId(userId) map {
        comments => if( comments.isEmpty) {
          NotFound("no comments found for user")
        } else {
          Ok(Json.toJson(comments))
        }
      }
    } catch {
      case ex: Exception => Future.successful(InternalServerError("Unknown error occurred"))
    }
  }
  }

  def updateComment(id: String): Action[AnyContent] = authWrapper.async { implicit request => {
    try {
      val userId = request.userInfo.userID

      CommentForm.form.bindFromRequest.fold(
        formErrors => {
          formErrors.errors.foreach(println)
          Future.successful(BadRequest("some credentials are missing"))
        },
        data => {
          uuid.isValid(id)

          commentDAL.getCommentById(id) flatMap {
            comment => {
              if (comment.isEmpty) {
                Future.successful(NotFound("Comment is not found"))
              } else {
                if (comment.get.reviewer_id != userId) {
                  Future.successful(Forbidden("no permission"))
                } else {
                  commentDAL.updateComment(id, data.content) map {
                    res => Ok(Json.toJson(Comment(id, data.content, userId, comment.get.article_id, comment.get.no_of_likes)))
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

  def deleteComment(id: String): Action[AnyContent] = authWrapper.async { implicit request => {
    try {
      val userId = request.userInfo.userID

      uuid.isValid(id)
      commentDAL.getCommentById(id) flatMap {
        comment => {
          if (comment.isEmpty) {
            Future.successful(NotFound("Comment is not found"))
          } else {
            if (comment.get.reviewer_id != userId) {
              Future.successful(Forbidden("no permission"))
            } else {
              articleDAL.getArticleById(comment.get.article_id) flatMap  {
                article => commentDAL.deleteComment(id, article.get) map {
                  res => NoContent
                }
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
