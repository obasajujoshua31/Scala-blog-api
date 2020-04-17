package dal

import javax.inject.Inject
import models.{Article, ArticleTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

 class ArticleDAL @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) (implicit executionContext: ExecutionContext)  extends HasDatabaseConfigProvider[JdbcProfile]{

  var articleList = TableQuery[ArticleTable]

  def createArticle(article: Article) :Future[String] = {
    article.no_of_comments = 0
    article.no_of_likes = 0

    dbConfig.db.run(
      articleList += (article)).
      map(res => res.toString).
      recover {
        case ex: Exception => {
          ex.getMessage
        }
      }
  }

   def getAllArticles: Future[Seq[Article]] = {
     dbConfig.db.run(
       articleList.result
     )
   }

   def getArticleById(id: String): Future[Option[Article]] = {
     dbConfig.db.run(
       articleList.
         filter(_.id === id).
         result.headOption
     )
   }

   def getArticlesByUserId(author_id: String): Future[Seq[Article]]  = {
     dbConfig.db.run(
       articleList.
         filter(_.author_id === author_id).
         result
     )
   }

   def updateArticle(article: Article): Future[Int] = {
     dbConfig.db.run(
       articleList.
         filter(_.id === article.id).
         map(
       x => (x.title, x.content))
         .update(article.title, article.content)
     )
   }

   def deleteArticle(id: String): Future[Int] = {
     dbConfig.db.run(
       articleList.filter(_.id === id).delete
     )
   }
}
