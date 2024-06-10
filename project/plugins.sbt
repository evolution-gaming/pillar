externalResolvers := Seq(
  Resolver.url("Evolution Gaming (ivy)", url("https://rms.evolutiongaming.com/pub-ivy/"))(
    Resolver.ivyStylePatterns
  )
)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.12")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.11")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")