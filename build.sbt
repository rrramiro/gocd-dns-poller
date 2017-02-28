import Dependencies._

name := "gocd-dns-poller"

organization := "fr.ramiro"

scalaVersion := "2.12.1"

version      := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  scalaTest % Test,
  "cd.go.plugin" % "go-plugin-api" % "17.2.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "dnsjava" % "dnsjava" % "2.1.8"
)
