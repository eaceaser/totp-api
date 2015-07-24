package com.tehasdf.totpapi.otp

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Base32

class OTP(codeLength: Int) {
  private val mod = BigInt(10).pow(codeLength)
  private val b32 = new Base32()

  def withSecret(base32Secret: String): BoundOTPSource = {
    val decoded = b32.decode(base32Secret)
    new BoundOTPSource(mod.longValue(), decoded)
  }
}

object BoundOTPSource {
  private val HMACSHA1 = "HmacSHA1"

  def convertToRFCBytes(input: Long) = {
    val out = ByteBuffer.allocate(8)
    for (i <- 0.until(8).reverse) {
      out.put((input >>> (8 * i)).toByte)
    }
    out.array()
  }
}

class BoundOTPSource(mod: Long, secretBytes: Array[Byte]) {
  import BoundOTPSource._

  val secret = new SecretKeySpec(secretBytes, HMACSHA1)
  val mac = Mac.getInstance(HMACSHA1)
  mac.init(secret)

  def apply(input: Long): Long = {
    val bytes = mac.doFinal(convertToRFCBytes(input))
    val offset = bytes.last & 0xF
    val bb = ByteBuffer.allocate(4)
    bb.put((bytes(offset) & 0x7F).toByte)
    bb.put((bytes(offset+1) & 0xFF).toByte)
    bb.put((bytes(offset+2) & 0xFF).toByte)
    bb.put((bytes(offset+3) & 0xFF).toByte)
    val code = BigInt(bb.array()) // we always know we have an array
    (code % mod).longValue()
  }
}
