package com.tehasdf.totpapi

import akka.actor.{Actor, Props, ActorSystem}
import akka.io.IO
import com.tehasdf.totpapi.config.{OTPConfig, LDAPConfig}
import com.tehasdf.totpapi.factory.LDAPConnectionFactory
import com.tehasdf.totpapi.otp.{OTPGenerator, OTP, TOTP}
import com.typesafe.config.ConfigFactory
import spray.can.Http


class APIImpl(override val authHandler: TOTPAuthHandler) extends Actor with Api {
  def actorRefFactory = context
  def receive = {
    runRoute(api)
  }
}

object TotpApiApp extends App {
  val config = ConfigFactory.load()

  implicit val system = ActorSystem("totp-api")
  implicit val c = system.dispatcher

  val otpConfig = new OTPConfig(config)
  val otp = new OTP(otpConfig.otpCodeDigits)
  val totp = new TOTP(otpConfig.totpIntervalSecs.toInt, otp)
  val gen = new OTPGenerator(otpConfig.otpSecretBytes, otpConfig.otpNumScratchCodes, otpConfig.otpScratchCodeBytes, otpConfig.otpScratchCodeDigits)
  val ldap = new LDAPConnectionFactory(new LDAPConfig(config))
  val authHandler = new TOTPAuthHandler(totp, gen, otpConfig.otpCodeDigits, otpConfig.otpScratchCodeDigits, (u,p) => {ldap.bindAsUser(u,p).map( _ => true).recover { case _ => false }}, () => { ldap.bindAsAdmin().map { _.userFactory() } })

  val service = system.actorOf(Props(classOf[APIImpl], authHandler), "totp-api")
  IO(Http) ! Http.Bind(service, "localhost", port=9910)
}
