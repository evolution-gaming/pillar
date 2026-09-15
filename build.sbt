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

ThisBuild / crossScalaVersions := Seq("2.13.18", "3.3.8")
ThisBuild / scalaVersion := crossScalaVersions.value.head

lazy val compilerOpts = Seq(
  scalacOptions ++= Seq(
    "-release:17",
    "-deprecation",
  ),
  scalacOptions ++= crossSettings(
    scalaVersion = scalaVersion.value,
    // Good compiler options for Scala 2.13 are coming from com.evolution:sbt-scalac-opts-plugin:0.2.0,
    // but its support for Scala 3 is limited, especially what concerns linting options.
    //
    // If Scala 3 is made the primary target, good linting scalac options for it should be added first.
    if3 = Seq(
      "-Ykind-projector:underscores",

      // disable new brace-less syntax:
      // https://alexn.org/blog/2022/10/24/scala-3-optional-braces/
      "-no-indent",

      // improve error messages:
      "-explain",
      "-explain-types",
      "-feature",

      // used in ConnectionConfiguration
      "-language:implicitConversions",
    ),
    if2 = Seq(
      "-Xsource:3",
    ),
  ),
  Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-no-link-warnings",
  ),
  Compile / doc / scalacOptions ++= crossSettings(
    scalaVersion = scalaVersion.value,
    // "-implicits" is a Scaladoc 2 option, Scala 3's Scaladoc ignores it with a warning
    if3 = Nil,
    if2 = Seq("-implicits"),
  ),
  Compile / doc / scalacOptions -= "-Xfatal-warnings",
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "pillar",
    description := "Scala library which manages migrations for your Cassandra data stores",

    compilerOpts,

    // to ensure that testcontainers are stopped after the test run (Ryuk kills them on JVM exit)
    Test / fork := true,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.9",
      "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.5",
      "org.testcontainers" % "testcontainers-cassandra" % "2.0.5" % Test,
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % Test,
      "ch.qos.logback" % "logback-classic" % "1.6.3" % Test,
    ),
  )

addCommandAlias("fmt", "+all scalafmtRepo")
addCommandAlias("build", "+all scalafmtCheckRepo versionPolicyCheck Compile/doc test")

def crossSettings[T](scalaVersion: String, if3: T, if2: T): T = {
  scalaVersion match {
    case version if version.startsWith("3") => if3
    case _ => if2
  }
}
