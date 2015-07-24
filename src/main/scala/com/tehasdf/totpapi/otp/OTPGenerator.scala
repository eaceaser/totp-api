package com.tehasdf.totpapi.otp

import java.nio.ByteBuffer
import java.security.SecureRandom

import org.apache.commons.codec.binary.Base32

import scala.annotation.tailrec
import scala.collection.AbstractIterator

class OTPGenerator(secretBytes: Int, numScratchCodes: Int, scratchCodeBytes: Int, scratchCodeDigits: Int) {
  val rand = new SecureRandom()

  private val scratchMod = BigInt(10).pow(scratchCodeDigits)
  private val b32 = new Base32()

  class ScratchIterator extends AbstractIterator[BigInt] {
    override def hasNext: Boolean = true

    @tailrec
    override final def next(): BigInt = {
      val code = new Array[Byte](scratchCodeBytes)
      rand.nextBytes(code)
      val buf = ByteBuffer.wrap(code)
      val rv = (BigInt(code) & 0x7FFFFFFF) % scratchMod

      if (rv >= scratchMod / 10) {
        rv.longValue()
      } else {
        next()
      }
    }
  }

  val scratchIt = new ScratchIterator

  def newSecret() = {
    val buf = new Array[Byte](secretBytes)
    val rand = new SecureRandom()
    rand.nextBytes(buf)
    b32.encodeToString(buf)
  }

  def newScratchCodes(): Set[String] = {
    (scratchIt take numScratchCodes).map(_.toString).toSet
  }
}
