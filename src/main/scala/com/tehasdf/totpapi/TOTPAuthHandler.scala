package com.tehasdf.totpapi

import java.time.Instant

import com.tehasdf.totpapi.factory.LDAPUserFactory
import com.tehasdf.totpapi.model.User
import com.tehasdf.totpapi.otp.{OTPGenerator, TOTP}

import scala.concurrent.{ExecutionContext, Future}

class TOTPAuthHandler(totp: TOTP, gen: OTPGenerator, tokenDigits: Int, scratchCodeDigits: Int,
                      userAuthenticator: (String, String) => Future[Boolean],
                      userProvider: () => Future[LDAPUserFactory])(implicit val execution: ExecutionContext) {
  def verify(user: String, input: String): Future[Boolean] = {
    val (password, code) = (if (input.length >= scratchCodeDigits) {
      val (passwordToAttempt, scratchToAttemptStr) = input.splitAt(input.size - scratchCodeDigits)
      try { Some((passwordToAttempt, scratchToAttemptStr.toLong.toString)) } catch { case ex: NumberFormatException => None }
    } else None).getOrElse {
      input.splitAt(input.size - tokenDigits)
    }

    userAuthenticator(user, password).flatMap { v =>
      if (!v) {
        Future.successful(false)
      } else {
        val up = userProvider()
        up.flatMap {
          _.getUser(user)
        } flatMap { u =>
          if (u.totpScratchCodes.contains(code)) {
            up.flatMap { _.invalidateScratchCode(user, code) } .map { _ => true }
          } else {
            val now = Instant.now()
            val generator = totp.withSecret(u.totpSecret)
            val codes = Seq(now.minusSeconds(30), now, now.plusSeconds(30)).map(generator.at(_))
            val codeSet = codes.map(c => String.format("%06d", java.lang.Long.valueOf(c))).toSet

            Future.successful(codeSet.contains(code))
          }
        }
      }
    }
  }

  def createNewSecret(): String = gen.newSecret()

  def provisionNewUser(username: String, secret: String, token: Long): Future[User] = {
    val generated = totp.withSecret(secret).now()
    if (generated == token) {
      val user = User(secret, gen.newScratchCodes())
      userProvider().flatMap(_.setUser(username, user)).map(_ => user)
    } else {
      Future.failed(new RuntimeException("Token did not validate."))
    }
  }

  def deprovisionUser(username: String): Future[Boolean] = {
    userProvider().flatMap(_.disableUser(username)).map(_ => true)
  }
}
