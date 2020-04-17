package utils

import java.util.UUID
import scala.util.Success
import scala.util.matching.Regex

case class UUIDValidationException(exception: String) extends Exception(exception)

class Uuid {
  val uuidRegex: Regex = """^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$""".r

  def generate: String = {
     UUID.randomUUID().toString
   }

  def isValid(uuid: String): Success.type = {
    uuid match {
      case uuidRegex(_*) => Success
      case _ => throw  UUIDValidationException("UUID is not valid")
    }
  }
}
