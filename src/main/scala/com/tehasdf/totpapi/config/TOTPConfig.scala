package com.tehasdf.totpapi.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

class OTPConfig(underlying: Config) {
  val otpCodeDigits = underlying.getInt("otp.codeDigits")
  val totpIntervalSecs  = underlying.getDuration("totp.interval", TimeUnit.SECONDS)

  val otpSecretBytes = underlying.getInt("otp.generate.secretBytes")
  val otpNumScratchCodes = underlying.getInt("otp.generate.numScratchCodes")
  val otpScratchCodeBytes = underlying.getInt("otp.generate.scratchCodeBytes")
  val otpScratchCodeDigits = underlying.getInt("otp.generate.scratchCodeDigits")
}
