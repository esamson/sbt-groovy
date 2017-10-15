sbtPlugin := true

name := "sbt-groovy"

organization := "org.softnetwork.sbt.plugins"

version := "0.1.4-SNAPSHOT"

scalaVersion := "2.12.3"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/fupelaqu/sbt-groovy</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:fupelaqu/sbt-groovy.git</url>
    <connection>scm:git:git@github.com:fupelaqu/sbt-groovy.git</connection>
  </scm>
  <developers>
    <developer>
      <id>smanciot</id>
      <name>Stéphane Manciot</name>
      <url>http://www.linkedin.com/in/smanciot</url>
    </developer>
  </developers>)

//ScriptedPlugin.scriptedSettings

scriptedLaunchOpts ++= Seq(
  "-Xmx2048M", 
  "-XX:MaxMetaspaceSize=512M",
  s"-Dplugin.version=${version.value}"
)

scriptedBufferLog := false

