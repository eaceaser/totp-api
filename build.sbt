name := "totp-api"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "io.spray" %% "spray-io" % "1.3.3",
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.unboundid" % "unboundid-ldapsdk" % "3.0.0",
  "commons-codec" % "commons-codec" % "1.10",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)
