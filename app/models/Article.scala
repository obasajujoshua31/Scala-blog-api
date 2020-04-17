package models
import play.api.data.Form
import play.api.data.Forms._
import slick.jdbc.MySQLProfile.api._

case class Article(var id: String, var content:String, var title: String, var created_at: String, var author_id: String, var no_of_comments: Int=0, var no_of_likes: Int=0)

case class ArticleFormat(content: String, title: String)

object ArticleForm {
  var form: Form[ArticleFormat] = Form(
    mapping(
      "content" -> nonEmptyText,
      "title" -> nonEmptyText,
    )(ArticleFormat.apply)(ArticleFormat.unapply)
  )
}

class ArticleTable(tag: Tag) extends Table[Article](tag, "articles") {
  val users = TableQuery[UserTable]

  def id = column[String]("id", O.PrimaryKey, O.Unique)
  def content = column[String]("content")
  def title = column[String]("title")
  def created_at = column[String]("created_at")
  def author_id = column[String]("author_id")
  def no_of_comments = column[Int]("no_of_comments")
  def no_of_likes = column[Int]("no_of_likes")

  def user = foreignKey("USER_FK", author_id, users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  override def * = (id, content, title, created_at, author_id, no_of_comments, no_of_likes) <> ((Article.apply _).tupled, Article.unapply)
}