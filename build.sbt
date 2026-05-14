import sbt.*
import sbt.Keys.*

ThisBuild / organization := "com.evolutiongaming"

ThisBuild / startYear := Some(2020)
ThisBuild / homepage := Some(url("https://github.com/evolution-gaming/pillar"))
ThisBuild / licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

ThisBuild / organizationName := "Evolution"
ThisBuild / organizationHomepage := Some(url("https://evolution.com"))

// Maven Central requires <developers> in published pom.xml files
// migesok: not sure what to put here
ThisBuild / developers := List(
  Developer(
    id = "migesok",
    name = "Mikhail Sokolov",
    email = "mikhail.g.sokolov@gmail.com",
    url = url("https://github.com/migesok"),
  ),
)

ThisBuild / scmInfo := Some(ScmInfo(
  browseUrl = url("https://github.com/evolution-gaming/pillar"),
  connection = "git@github.com:evolution-gaming/pillar.git",
))

// DO NOT CHANGE THIS SETTING UNLESS YOU FULLY UNDERSTAND THE CONSEQUENCES!
//
// WARNING: BinaryCompatible is used instead of BinaryAndSourceCompatible because BinaryAndSourceCompatible fails
// on new methods added to objects, which doesn't really break neither source, nor binary compatibility.
// So the source compatibility should be guaranteed manually.
// TODO: WIP return BinaryCompatible after the first maven central release
ThisBuild / versionPolicyIntention := Compatibility.None

// TODO: update Scala versions
ThisBuild / crossScalaVersions := Seq("2.13.14", "3.3.3")
ThisBuild / scalaVersion := crossScalaVersions.value.head

// TODO: better compiler options
ThisBuild / scalacOptions ++= Seq("-language:implicitConversions")

lazy val root = project
  .in(file("."))
  .settings(
    name := "pillar",
    description := "Scala library which manages migrations for your Cassandra data stores",

    Test / fork := true, // TODO: remove once tests migrated to testcontainers?

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.3",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.8.0",
      "org.cassandraunit" % "cassandra-unit" % "3.11.2.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
      "com.google.guava" % "guava" % "18.0" % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
    ),
  )

addCommandAlias("fmt", "+all scalafmtAll scalafmtSbt")
addCommandAlias("build", "+all scalafmtCheckAll scalafmtSbtCheck versionPolicyCheck Compile/doc test")
