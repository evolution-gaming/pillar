import Keys._
import sbt._

lazy val root = project
  .in(file("."))
  .settings(
    name := "pillar",
    scalaVersion := crossScalaVersions.value.head,
    crossScalaVersions := Seq("2.13.1", "2.12.11"),
    licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
    resolvers += "Evolution Gaming repository" at "https://rms.evolutiongaming.com/public/",
    resolvers += "Confluent" at "https://packages.confluent.io/maven",
    libraryDependencies ++= dependencies,

    organization := "com.evolutiongaming",
    organizationName := "Evolution Gaming",
    organizationHomepage := Some(url("http://evolutiongaming.com")),
    homepage := Some(url("http://github.com/evolution-gaming/pillar")),
    startYear := Some(2020),

    Test / fork := true,
    releaseCrossBuild := true,
  )

val dependencies = Seq(
  "com.typesafe" % "config" % "1.4.3",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.12.0",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.5",
  "org.cassandraunit" % "cassandra-unit" % "3.11.2.0" % Test,
  "org.mockito" % "mockito-core" % "5.11.0" % Test,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.mockito" %% "mockito-scala" % "1.17.31" % Test,
  "com.google.guava" % "guava" % "18.0" % Test,
  "ch.qos.logback" % "logback-classic" % "1.5.6" % Test
)