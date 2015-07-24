package com.tehasdf.totpapi.factory

import com.tehasdf.totpapi.config.{SchemaInfo, SearchInfo}
import com.tehasdf.totpapi.model.User
import com.unboundid.ldap.sdk._

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

class LDAPUserFactory(ldap: LDAPConnection, searchInfo: SearchInfo, schemaInfo: SchemaInfo)(implicit val execution: ExecutionContext) {
  def getUser(username: String) = {
    lookupEntry(username).map { entry =>
      val secret = Option(entry.getAttributeValue("totpSecret")).getOrElse(throw new RuntimeException("Could not read TOTP information for user."))
      val scratchCodes = entry.getAttributeValues("totpScratchCode")
      User(secret, scratchCodes.toSet)
    }
  }

  def setUser(username: String, user: User): Future[LDAPResult] = {
    lookupEntry(username) flatMap { entry =>
      val dn = entry.getDN
      val modifications =
        new Modification(ModificationType.REPLACE, schemaInfo.scratch, user.totpScratchCodes.map(_.toString).toSeq: _*) ::
        new Modification(ModificationType.REPLACE, schemaInfo.enabled, "TRUE") ::
        new Modification(ModificationType.REPLACE, schemaInfo.secret, user.totpSecret) :: Nil

      Future {
        val resp = ldap.modify(dn, modifications.toList)
        if (resp.getResultCode != ResultCode.SUCCESS) {
          throw new RuntimeException("User update failed.")
        }
        resp
      }
    }
  }

  def invalidateScratchCode(username: String, code: String): Future[LDAPResult] = {
    lookupEntry(username) flatMap { entry =>
      val dn = entry.getDN
      val modification = new Modification(ModificationType.DELETE, schemaInfo.scratch, code)
      Future {
        val resp = ldap.modify(dn, modification)
        if (resp.getResultCode != ResultCode.SUCCESS) {
          throw new RuntimeException("Failed to invalidate scratch code.")
        }
        resp
      }
    }
  }

  def disableUser(username: String): Future[LDAPResult] = {
    lookupEntry(username) flatMap { entry =>
      val dn = entry.getDN
      val modifications =
        new Modification(ModificationType.DELETE, schemaInfo.secret) ::
        new Modification(ModificationType.DELETE, schemaInfo.scratch) ::
        new Modification(ModificationType.REPLACE, schemaInfo.enabled, "FALSE") :: Nil

      Future {
        val resp = ldap.modify(dn, modifications)
        if (resp.getResultCode != ResultCode.SUCCESS) {
          throw new RuntimeException("Failed to disable user's TOTP.")
        }
        resp
      }
    }
  }

  private def lookupEntry(username: String) = {
    val req = new SearchRequest(searchInfo.baseDN, SearchScope.ONE, Filter.createEqualityFilter(searchInfo.userAttribute, username))
    Future {
      val result = ldap.search(req)
      if (result.getEntryCount != 1) {
        throw new RuntimeException("Could not find user record.")
      }

      result.getSearchEntries.head
    }
  }
}
