package models
import play.api.data.Form
import play.api.data.Forms._
import slick.jdbc.MySQLProfile.api._

case class Comment(var id: String, var content:String, var reviewer_id: String, var article_id: String, var no_of_likes: Int = 0 )

case class CommentFormat(content: String)

object CommentForm {
  var form: Form[CommentFormat] = Form(
    mapping(
      "content" -> nonEmptyText,
    )(CommentFormat.apply)(CommentFormat.unapply)
  )
}


class CommentTable(tag: Tag) extends Table[Comment](tag, "comments") {
  val users = TableQuery[UserTable]
  var articles = TableQuery[ArticleTable]

  def id = column[String]("id", O.PrimaryKey, O.Unique)
  def content = column[String]("content")
  def reviewer_id = column[String]("reviewer_id")
  def article_id = column[String]("article_id")
  def no_of_likes = column[Int]("no_of_likes")

  def user = foreignKey("USER_COMMENT_FK", reviewer_id, users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def article = foreignKey("ARTICLE_COMMENT_FK", article_id, articles)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  override def * = (id, content, reviewer_id, article_id, no_of_likes) <>  ((Comment.apply _).tupled, Comment.unapply)
}