package models
import slick.jdbc.MySQLProfile.api._

case class Like(var id: String, var reactor_id:String, var comment_id: String = "", var article_id: String = "")


class LikeTable(tag: Tag) extends Table[Like](tag, "likes") {
  val users = TableQuery[UserTable]
  val articles = TableQuery[ArticleTable]
  var comments = TableQuery[CommentTable]

  def id = column[String]("id", O.PrimaryKey, O.Unique)
  def reactor_id = column[String]("reactor_id")
  def article_id = column[String]("article_id")
  def comment_id = column[String]("comment_id")

  def user = foreignKey("USER_LIKE_FK", reactor_id, users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def article = foreignKey("ARTICLE_LIKE_FK", article_id, articles)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def comment = foreignKey("COMMENT_LIKE_FK", comment_id, comments)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  override def * = (id, reactor_id, comment_id, article_id) <> (Like.tupled, Like.unapply)
}