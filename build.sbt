lazy val root = (project in file(".")).settings(
  inThisBuild(
    Seq(
      organization := "ph.samson",
      version := "0.2.0-SNAPSHOT",
      homepage := Some(url("https://github.com/esamson/sbt-groovy")),
      licenses := Seq(
        "MIT" -> url("http://opensource.org/licenses/mit-license.php")),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/esamson/sbt-groovy"),
          "scm:git:git@github.com:esamson/sbt-groovy.git"
        )
      ),
      developers := List(
        Developer(
          id = "esamson",
          name = "Edward Samson",
          email = "edward@samson.ph",
          url = url("https://edward.samson.ph")
        )
      ),
      scalaVersion := "2.12.4"
    )),
  name := "sbt-groovy",
  sbtPlugin := true,
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  scriptedLaunchOpts ++= Seq(
    "-Xmx2048M",
    "-XX:MaxMetaspaceSize=512M",
    s"-Dplugin.version=${version.value}"
  ),
  scriptedBufferLog := false
)
