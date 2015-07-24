package com.tehasdf.totpapi.config

import com.typesafe.config.Config

case class TrustStore(path: String, password: String)

case class SearchInfo(baseDN: String, userAttribute: String)

case class SchemaInfo(secret: String, scratch: String, enabled: String)

class LDAPConfig(underlying: Config) {
  val uri = underlying.getString("ldap.uri")
  val startTLS = underlying.getBoolean("ldap.startTLS")
  val trustStore = if (startTLS) {
    val trustStoreResourcePath = underlying.getString("ldap.truststore.path")
    val trustStorePassword = underlying.getString("ldap.truststore.password")
    Some(TrustStore(trustStoreResourcePath, trustStorePassword))
  } else {
    None
  }
  val searchInfo = SearchInfo(
    underlying.getString("ldap.base"),
    underlying.getString("ldap.bindAttribute"))

  val adminUsername = underlying.getString("ldap.adminUser")
  val adminPassword = underlying.getString("ldap.adminPassword")

  val schemaInfo = SchemaInfo(
    underlying.getString("ldap.schema.secret"),
    underlying.getString("ldap.schema.scratch"),
    underlying.getString("ldap.schema.enabled")
  )
}
