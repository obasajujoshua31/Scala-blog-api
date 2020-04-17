package dal

import javax.inject.Inject
import models.{User, UserTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._
import utils._

import scala.concurrent.{ExecutionContext, Future}

class UserDAL @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, val password: Password) (implicit executionContext: ExecutionContext)  extends HasDatabaseConfigProvider[JdbcProfile]{

      var userList = TableQuery[UserTable]

     def createUser(user: User) :Future[String] = {
       user.password = password.hashPassword(user.password)

           dbConfig.db.run(userList += user).
             map(res => res.toString).
             recover {
                   case ex: Exception => {
                         println(ex.getMessage)
                         ex.getMessage
                   }
             }
     }

      def findUserByEmail(email:String) : Future[Option[User]] = {
            dbConfig.db.run(userList.filter(_.email === email).result.headOption)
      }

  def findUserByID(id:String) : Future[Option[User]] = {
    dbConfig.db.run(userList.filter(_.id === id).result.headOption)
  }
}
