sbtPlugin := true

name := "sbt-groovy"

organization := "ph.samson"

version := "0.2.0-SNAPSHOT"

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
  <url>https://github.com/esamson/sbt-groovy</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:esamson/sbt-groovy.git</url>
    <connection>scm:git:git@github.com:esamson/sbt-groovy.git</connection>
  </scm>
  <developers>
    <developer>
      <id>esamson</id>
      <name>Edward Samson</name>
      <url>https://edward.samson.ph</url>
    </developer>
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

