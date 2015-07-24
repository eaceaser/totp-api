package com.tehasdf.totpapi.otp

import java.time.Instant
import java.util.Date

class TOTP(intervalSecs: Int, otp: OTP) {
  def withSecret(secret: String): BoundTOTPSource = {
    new BoundTOTPSource(intervalSecs, otp.withSecret(secret))
  }
}

class BoundTOTPSource(intervalSecs: Int, otp: BoundOTPSource) {
  val intervalMillis = intervalSecs * 1000
  def at(i: Instant) = {
    otp.apply(i.toEpochMilli / (intervalMillis))
  }

  def now() = {
    otp.apply(new Date().getTime / (intervalSecs * 1000))
  }
}
