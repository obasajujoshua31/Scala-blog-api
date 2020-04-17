package models
import play.api.data.Form
import play.api.data.Forms._
import slick.jdbc.MySQLProfile.api._

case class User(var id: String, var name:String, var email: String, var password: String)

case class UserFormat(name: String, email: String, password: String)
case class UserLogin(email: String, password: String)
case class UserInfo(userID: String, nbf: String)

object UserForm {
  var form: Form[UserFormat] = Form(
    mapping(
      "name" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
    )(UserFormat.apply)(UserFormat.unapply)
  )
}

object UserLoginFormat {
  var form: Form[UserLogin] = Form (
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(UserLogin.apply)(UserLogin.unapply)
  )
}


class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[String]("id", O.PrimaryKey, O.Unique)
  def name = column[String]("name")
  def email = column[String]("email")
  def password = column[String]("password")

  override def * = (id, name, email, password) <> (User.tupled, User.unapply)
}