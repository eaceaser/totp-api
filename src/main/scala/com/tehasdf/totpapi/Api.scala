package com.tehasdf.totpapi

import spray.http.{FormData, StatusCodes}
import spray.httpx.SprayJsonSupport
import spray.json._
import spray.routing.HttpService

import scala.util.{Success, Failure}

trait Api extends HttpService with SprayJsonSupport with AdditionalFormats {
  implicit def executionContext = actorRefFactory.dispatcher

  val authHandler: TOTPAuthHandler

  val api =
    path("generate") {
      get { ctx =>
        val secret = authHandler.createNewSecret()
        ctx.complete(
          JsObject(
            "secret" -> JsString(secret)
          ))
      }
    } ~
    path("provision") {
      post {
        entity(as[JsObject]) { data =>
          val user = data.fields("user").asInstanceOf[JsString].value
          val secret = data.fields("secret").asInstanceOf[JsString].value
          val token = data.fields("token").asInstanceOf[JsNumber].value.toLong
          val f = authHandler.provisionNewUser(user, secret, token)
          onComplete(f) {
            case Success(result) =>
              complete(JsObject(
                "scratch" -> JsArray(result.totpScratchCodes.map { JsString(_) }.toVector)))
            case Failure(ex) =>
              complete(StatusCodes.Unauthorized, "Failed to provision: " + ex.toString)
          }
        }
      }
    } ~
    path("deprovision") {
      post {
        entity(as[JsObject]) { data =>
          val user = data.fields("user").asInstanceOf[JsString].value
          val f = authHandler.deprovisionUser(user)
          onComplete(f) {
            case Success(result) =>
              complete(JsObject(Nil))
            case Failure(ex) =>
              complete(StatusCodes.Unauthorized, "Failed to deprovision: "+ex.toString)
          }
        }
      }
    } ~
    path("pam") {
      // support pam-url
      post {
        entity(as[FormData]) { data =>
          val map = Map(data.fields: _*)
          val f = authHandler.verify(map("user"), map("token"))
          onSuccess(f) { result =>
            if (result) {
              complete("OK")
            } else {
              complete(StatusCodes.Unauthorized, "UNAUTHORIZED")
            }
          }
        }
      }
    }
}
