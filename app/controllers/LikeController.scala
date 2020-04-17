package controllers

import dal._
import javax.inject._
import models.Like
import play.api.libs.json._
import play.api.mvc._
import utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class LikeController @Inject()(cc: ControllerComponents, articleDAL: ArticleDAL, commentDAL: CommentDAL, jwt: JWT, likesDAL: LikesDAL, authWrapper: AuthWrapper, userDAL: UserDAL, uuid: Uuid) extends AbstractController(cc) {
  implicit val likeWrites: Writes[Like] = (o: Like) => {
    Json.obj(
      "id" -> o.id,
      "reactor_id" -> o.reactor_id,
      "comment_id"-> o.comment_id,
      "article_id" -> o.article_id,
    )
  }

  def likeOrUnlikeArticle(articleId: String): Action[AnyContent] = authWrapper.async { implicit request =>

    try {
      uuid.isValid(articleId)
          val userId = request.userInfo.userID

          articleDAL.getArticleById(articleId) flatMap {
            article => {
              if (article.isEmpty) {
                Future.successful(BadRequest("article is not found"))
              } else {
                 likesDAL.getLikesForUserAndArticle(userId, articleId) flatMap {
                   like => if (like.isEmpty) {
                     val articleLike = Like(uuid.generate, userId, "", articleId)
                     likesDAL.createArticleLikes(articleLike, article.get) map {
                       res => Ok(Json.toJson(articleLike))
                     } recover {
                       res => BadRequest(res.getMessage)
                     }
                   } else {
                     likesDAL.removeLikeOnArticle(userId, article.get) map {
                       res => NoContent
                     }
                   }
                 }
              }
            }
          }
    } catch {
      case ex: UUIDValidationException => Future.successful(BadRequest(s"$articleId is not valid"))
      case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
    }
  }

  def likeOrUnlikeComment(commentId: String): Action[AnyContent] = authWrapper.async { implicit request =>

    try {
      uuid.isValid(commentId)
      val userId = request.userInfo.userID

      commentDAL.getCommentById(commentId) flatMap {
        comment => {
          if (comment.isEmpty) {
            Future.successful(BadRequest("comment is not found"))
          } else {
            likesDAL.getLikesForUserAndComment(userId, commentId) flatMap {
              like => if (like.isEmpty) {
                val commentLike = Like(uuid.generate, userId, commentId)
                likesDAL.createCommentLikes(commentLike, comment.get) map {
                  res => Ok(Json.toJson(commentLike))
                }
              } else {
                likesDAL.removeLikeOnComment(userId, comment.get) map {
                  res => NoContent
                }
              }
            }
          }
        }
      }
    } catch {
      case ex: UUIDValidationException => Future.successful(BadRequest(s"$commentId is not valid"))
      case exception: Exception => Future.successful(InternalServerError(exception.getMessage))
    }
  }


  def loggedInUserLikes = authWrapper.async { implicit request => {
     try {
        val userId = request.userInfo.userID

       likesDAL.getLikesByUserId(userId) map {
         likes => if (likes.isEmpty) {
           NotFound("No likes found for use")
         } else {
           Ok(Json.toJson(likes))
         }
       }
     } catch {
       case ex: Exception => Future.successful(InternalServerError(ex.getMessage))
     }
  }
  }
}
