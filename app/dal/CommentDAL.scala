package dal

import javax.inject.Inject
import models.{Article, ArticleTable, Comment, CommentTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class CommentDAL @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit executionContext: ExecutionContext)  extends HasDatabaseConfigProvider[JdbcProfile]{

  var commentList = TableQuery[CommentTable]
  var articleList = TableQuery[ArticleTable]

  def createComment(comment: Comment, article: Article) :Future[Any] = {
    dbConfig.db.run(
      commentList += (comment)) map {
      res => {
        val query = for (article <- articleList if article.id === comment.article_id)
          yield (article.no_of_comments)
        dbConfig.db.run(query.update(article.no_of_comments + 1))
      }
    } recover {
        case ex: Exception => {
          ex.getMessage
        }
      }
  }

  def getAllCommentsOnArticleId(articleId: String): Future[Seq[Comment]] = {
    dbConfig.db.run(
      commentList.filter(_.article_id === articleId ).result
    )
  }

  def getCommentById(id: String): Future[Option[Comment]] = {
    dbConfig.db.run(
      commentList.
        filter(_.id === id).
        result.headOption
    )
  }

  def getCommentsByUserId(reviewer_id: String): Future[Seq[Comment]]  = {
    dbConfig.db.run(
      commentList.
        filter(_.reviewer_id === reviewer_id).
        result
    )
  }

  def updateComment(id: String, content: String): Future[Int] = {
    dbConfig.db.run(
      commentList.
        filter(_.id === id).
        map(
          x => (x.content))
        .update(content)
    )
  }

  def deleteComment(id: String, article: Article): Future[String] = {

    dbConfig.db.run(
      commentList.filter(_.id === id).delete) map {
      res => res.toString
    }

    val query = for (art <- articleList if art.id === article.id)
      yield (art.no_of_comments)
    dbConfig.db.run(query.update(article.no_of_comments - 1)) map {
      res => res.toString
    }
  }
}
