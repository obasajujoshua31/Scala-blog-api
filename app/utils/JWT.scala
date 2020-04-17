package utils

import java.time.Clock

import pdi.jwt.{Jwt, JwtAlgorithm, JwtJson}
import play.api.libs.json._

import scala.util.{Success, Try, Failure}

class JWT {
  implicit val clock: Clock = Clock.systemUTC
  val JWTSecretKey: String = "secretKey"

  val algo = JwtAlgorithm.HS512

  def createToken(payload: String): String = {
    val claim = Json.obj("userID" -> payload, "nbf" -> "1431520421")
    JwtJson.encode(claim, JWTSecretKey, algo)
  }

  def decodePayload(jwtToken: String) : Try[String] = {
      val decoded = Jwt.decodeRawAll(jwtToken,  JWTSecretKey, Seq(algo))

     decoded match  {
       case  Success(name) => Success(name._2)
       case Failure(exception) => {
         Failure(exception)
       }
        }
    }

  def isValidToken(jwtToken: String) :Boolean  ={
       JwtJson.isValid(jwtToken)
  }
}
