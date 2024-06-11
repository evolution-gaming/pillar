import Keys._
import sbt._

lazy val root = project
  .in(file("."))
  .settings(
    name := "pillar",
    scalaVersion := crossScalaVersions.value.head,
    crossScalaVersions := Seq("2.13.14", "2.12.19"),
    licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
    resolvers += Resolver.bintrayRepo("evolutiongaming", "maven"),
    libraryDependencies ++= dependencies,

    organization := "com.evolutiongaming",
    organizationName := "Evolution Gaming",
    organizationHomepage := Some(url("http://evolutiongaming.com")),
    homepage := Some(url("http://github.com/evolution-gaming/pillar")),
    startYear := Some(2020),

    Test / fork := true,
    bintrayOrganization := Some("evolutiongaming"),
    releaseCrossBuild := true,
  )

val dependencies = Seq(
  "com.typesafe" % "config" % "1.4.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.8.0",
  "org.cassandraunit" % "cassandra-unit" % "3.11.2.0" % Test,
  "org.mockito" % "mockito-core" % "3.3.0" % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.mockito" %% "mockito-scala" % "1.11.3" % Test,
  "com.google.guava" % "guava" % "18.0" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)