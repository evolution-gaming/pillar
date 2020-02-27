import Keys._
import sbt._
import xerial.sbt.Sonatype

fork in Test := true

val assemblyTestSetting = test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _ *)         => MergeStrategy.first
  case PathList(ps @ _ *) if ps.last endsWith ".html" => MergeStrategy.first
  case "META-INF/io.netty.versions.properties"        => MergeStrategy.last
  case "application.conf"                             => MergeStrategy.concat
  case "unwanted.txt"                                 => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val rhPackage = taskKey[File]("Packages the application for Red Hat Package Manager")
rhPackage := {
  val rootPath = new File(target.value, "staged-package")
    val subdirectories = Map(
      "bin" -> new File(rootPath, "bin"),
      "conf" -> new File(rootPath, "conf"),
      "lib" -> new File(rootPath, "lib")
    )
    subdirectories.foreach {
      case (_, subdirectory) => IO.createDirectory(subdirectory)
    }
    IO.copyFile(assembly.value, new File(subdirectories("lib"), "pillar.jar"))
    val bashDirectory = new File(sourceDirectory.value, "main/bash")
    bashDirectory.list.foreach {
      script =>
        val destination = new File(subdirectories("bin"), script)
        IO.copyFile(new File(bashDirectory, script), destination)
        destination.setExecutable(true, false)
    }
    val resourcesDirectory = new File(sourceDirectory.value, "main/resources")
    resourcesDirectory.list.foreach {
      resource =>
        IO.copyFile(new File(resourcesDirectory, resource), new File(subdirectories("conf"), resource))
    }
    val iterationId = try { sys.env("GO_PIPELINE_COUNTER") } catch { case e: NoSuchElementException => "DEV" }
    "fpm -f -s dir -t rpm --package %s -n pillar --version %s --iteration %s -a all --prefix /opt/pillar -C %s/staged-package/ .".format(target.value.getPath, version.value, iterationId, target.value.getPath)//.!

    val pkg = file("%s/pillar-%s-%s.noarch.rpm".format(target.value.getPath, version.value, iterationId))
    if(!pkg.exists()) throw new RuntimeException("Packaging failed. Check logs for fpm output.")
    pkg
}


val dependencies = Seq(
  "com.typesafe" % "config" % "1.3.1",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.4",
  "org.cassandraunit" % "cassandra-unit" % "3.1.3.2" % Test,
  "org.mockito" % "mockito-core" % "3.3.0" % Test,
  "org.scalatest" %% "scalatest" % "3.1.1" % Test,
  "org.mockito" %% "mockito-scala" % "1.11.2" % Test,
  "com.google.guava" % "guava" % "18.0" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)

lazy val root = project
  .in(file("."))
  .settings(name := "pillar")
  .settings(Defaults.coreDefaultSettings ++ Sonatype.sonatypeSettings)
  .settings(
    assemblyTestSetting,
    libraryDependencies ++= dependencies,
    name := "pillar",
    organization := "de.kaufhof",
    version := "4.1.2",
    homepage := Some(url("https://github.com/Galeria-Kaufhof/pillar")),
    licenses := Seq("MIT license" -> url(
      "http://www.opensource.org/licenses/mit-license.php")),
    scalaVersion := "2.12.10",
    crossScalaVersions := Seq("2.12.10", "2.13.1")
  )
  .settings(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    parallelExecution in Test := false,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ =>
      false
    },
    pomExtra := (
      <scm>
        <url>git@github.com:Galeria-Kaufhof/pillar.git</url>
        <connection>scm:git:git@github.com:Galeria-Kaufhof/pillar.git</connection>
      </scm>
        <developers>
          <developer>
            <id>marcopriebe</id>
            <name>MarcoPriebe</name>
            <url>https://github.com/MarcoPriebe</url>
          </developer>
          <developer>
            <id>lichtsprung</id>
            <name>Robert Giacinto</name>
            <url>https://github.com/lichtsprung</url>
          </developer>
          <developer>
            <id>adelafogoros</id>
            <name>Adela Fogoros</name>
            <url>https://github.com/adelafogoros</url>
          </developer>
          <developer>
            <id>muellenborn</id>
            <name>Markus Müllenborn</name>
            <url>https://github.com/muellenborn</url>
          </developer>
        </developers>
    )
  )
