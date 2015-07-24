package com.tehasdf.totpapi.model

case class User(totpSecret: String,
                totpScratchCodes: Set[String])
{ }
