import Keys._
import sbt._

lazy val root = project
  .in(file("."))
  .settings(
    name := "pillar",
    scalaVersion := crossScalaVersions.value.head,
    crossScalaVersions := Seq("2.13.14", "2.12.19", "3.3.3"),
    licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
    publishTo := Some(Resolver.evolutionReleases),
    libraryDependencies ++= dependencies,

    organization := "com.evolutiongaming",
    organizationName := "Evolution",
    organizationHomepage := Some(url("https://evolution.com")),
    homepage := Some(url("http://github.com/evolution-gaming/pillar")),
    startYear := Some(2020),

    Test / fork := true,
    releaseCrossBuild := true,
  )

Compile / scalacOptions ++= Seq("-language:implicitConversions")

val dependencies = Seq(
  "com.typesafe" % "config" % "1.4.3",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.8.0",
  "org.cassandraunit" % "cassandra-unit" % "3.11.2.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
  "com.google.guava" % "guava" % "18.0" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)

addCommandAlias("check", "show version")
