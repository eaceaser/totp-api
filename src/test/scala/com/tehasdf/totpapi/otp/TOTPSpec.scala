package com.tehasdf.totpapi.otp

import java.time.Instant

import org.scalatest.{Matchers, FlatSpec}

class TOTPSpec extends FlatSpec with Matchers {
  "TOTP" should "create a valid token" in {
    val o = new OTP(6)
    val t = new TOTP(30, o)

    val testInstant = Instant.ofEpochMilli(1437510277197L)
    t.withSecret("O2NETUUEMUK5PZON").at(testInstant) shouldBe 160 // verified with Google reference TOTP implementation
  }

  it should "generate valid tokens" in {
    val gen = new OTPGenerator(8, 5, 4, 8)

    val secret = gen.newSecret()
    val codes = gen.newScratchCodes()

    println(secret)
    println(codes)
  }
}
