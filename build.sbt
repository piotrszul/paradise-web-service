val ScalatraVersion = "2.6.5"

organization := "name.pszul"

name := "Paradise Web Service"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.neo4j.driver" % "neo4j-java-driver" % "1.7.2" % "compile",
  "org.scalatra" %% "scalatra" % ScalatraVersion,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
  "org.scalatra" %% "scalatra-json" % ScalatraVersion,
  "org.json4s"   %% "json4s-jackson" % "3.5.2",
  "com.typesafe" % "config" % "1.3.2" % "compile",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "provided",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "org.neo4j.test" % "neo4j-harness" % "3.5.3" % "test",
  "com.sun.jersey" % "jersey-core" % "1.19.3" % "test",
  "org.scalamock" %% "scalamock" % "4.1.0" % "test"
)

enablePlugins(ScalatraPlugin)

(scalastyleConfig) := baseDirectory.value / "build" / "scalastyle-config.xml"
