package utils
import com.github.t3hnar.bcrypt._

import scala.util.{Failure, Try, Success}



class Password {
  def hashPassword(password: String): String = {
    val salt = generateSalt

    val hash = password.bcrypt(salt)
    hash
  }

  def isMatchPassword(password: String, dbPassword: String): Try[Boolean] = Try[Boolean] {
      password.isBcryptedSafe(dbPassword) match {
        case Success(value) => value
        case  Failure(exception) => false
      }
  }
}
