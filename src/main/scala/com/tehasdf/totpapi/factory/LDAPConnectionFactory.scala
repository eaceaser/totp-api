package com.tehasdf.totpapi.factory

import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory

import com.tehasdf.totpapi.config.LDAPConfig
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPURL}
import com.unboundid.util.ssl.SSLUtil

import scala.concurrent.{ExecutionContext, Future}

class LDAPConnectionFactory(config: LDAPConfig)(implicit val execution: ExecutionContext) {
  private val bindFormat = config.searchInfo.userAttribute + "=%s," + config.searchInfo.baseDN
  private val startTLS = {
    if (config.startTLS) {
      val ts = KeyStore.getInstance(KeyStore.getDefaultType)
      ts.load(getClass.getResourceAsStream(config.trustStore.get.path), config.trustStore.get.password.toCharArray)
      val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
      tmf.init(ts)
      val sslUtil = new SSLUtil(tmf.getTrustManagers)
      val ssl = sslUtil.createSSLSocketFactory()
      Some(new StartTLSExtendedRequest(ssl))
    } else {
      None
    }
  }

  private val ldapURL = new LDAPURL(config.uri)

  def bindAsUser(username: String, password: String) = {
    Future {
      val conn = makeConn()
      conn.bind(bindFormat.format(username), password)
      new BoundLDAPConnection(conn, config)
    }
  }

  def bindAsAdmin() = {
    Future {
      val conn = makeConn()
      conn.bind(config.adminUsername, config.adminPassword)
      new BoundLDAPConnection(conn, config)
    }
  }

  private def makeConn() = {
    val conn = new LDAPConnection(ldapURL.getHost, ldapURL.getPort)
    startTLS.foreach(conn.processExtendedOperation(_))
    conn
  }
}

class BoundLDAPConnection(conn: LDAPConnection, config: LDAPConfig)(implicit val execution: ExecutionContext) {
  def userFactory() = {
    new LDAPUserFactory(conn, config.searchInfo, config.schemaInfo)
  }
}
