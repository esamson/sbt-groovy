addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.0.0")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.1+14-2cd81957")
libraryDependencies += { "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value }
