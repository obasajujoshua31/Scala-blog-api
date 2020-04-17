package dal

import javax.inject.Inject
import models.{Article, ArticleTable, Comment, CommentTable, Like, LikeTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class LikesDAL @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit executionContext: ExecutionContext)  extends HasDatabaseConfigProvider[JdbcProfile]{

  var commentList = TableQuery[CommentTable]
  var articleList = TableQuery[ArticleTable]
  var likeList = TableQuery[LikeTable]

  def createArticleLikes(like: Like, article: Article) :Future[Any] = {
    dbConfig.db.run(
      likeList += (like)) map {
      res => {
        val query = for (article <- articleList if article.id === like.article_id)
          yield (article.no_of_likes)
        dbConfig.db.run(query.update(article.no_of_likes + 1))
      }
    } recover {
      case ex: Exception => {
        throw new Exception(ex)
        ex.getMessage
      }
    }
  }

  def createCommentLikes(like: Like, comment: Comment) :Future[Any] = {
    dbConfig.db.run(
      likeList += (like)) map {
      res => {
        val query = for (comment <- commentList if comment.id === like.comment_id)
          yield (comment.no_of_likes)
        dbConfig.db.run(query.update(comment.no_of_likes + 1))
      }
    } recover {
      case ex: Exception => {
        ex.getMessage
      }
    }
  }

  def getAllLikesOnArticleId(articleId: String): Future[Seq[Like]] = {
    dbConfig.db.run(
      likeList.filter(_.article_id === articleId ).result
    )
  }

  def getLikesByUserId(reactor_id: String): Future[Seq[Like]]  = {
    dbConfig.db.run(
      likeList.
        filter(_.reactor_id === reactor_id).
        result
    )
  }

  def getLikesForUserAndArticle(reactor_id: String, article_id: String): Future[Option[Like]] = {
    val query = for (like <- likeList if (like.article_id === article_id && like.reactor_id === reactor_id))
      yield like

    dbConfig.db.run(query.result.headOption)
  }

  def getLikesForUserAndComment(reactor_id: String, comment_id: String): Future[Option[Like]] = {
    val query = for (like <- likeList if (like.comment_id === comment_id && like.reactor_id === reactor_id))
      yield like

    dbConfig.db.run(query.result.headOption)
  }


  def removeLikeOnArticle(reactor_id: String, article: Article): Future[String] = {
    val query = for (like <- likeList if (like.reactor_id === like.reactor_id && like.article_id === article.id))
      yield (like)
    runDeleteLike(query)

    val articleQuery =  for (art<- articleList if art.id === article.id)
      yield (art.no_of_likes)

    dbConfig.db.run(articleQuery.update(article.no_of_likes - 1)) map {
      res => res.toString
    }
  }

  def removeLikeOnComment(reactor_id: String, comment: Comment): Future[String] = {
    val query = for (like <- likeList if (like.reactor_id === like.reactor_id && like.comment_id === comment.id))
      yield (like)
    runDeleteLike(query)

    val commentQuery =  for (comm<- commentList if comm.id === comment.id)
      yield (comm.no_of_likes)

    dbConfig.db.run(commentQuery.update(comment.no_of_likes - 1)) map {
      res => res.toString
    }
  }

  private def runDeleteLike(query: Query[LikeTable, Like, Seq]): Unit = {
    dbConfig.db.run(query.delete)
  }
}

