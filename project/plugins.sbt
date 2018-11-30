addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1")
libraryDependencies += {
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
}
