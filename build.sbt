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
      scalaVersion := "2.12.4",
      pgpPublicRing := file("./travis/pubring.asc"),
      pgpSecretRing := file("./travis/secring.asc"),
    )),
  name := "sbt-groovy",
  sbtPlugin := true,
  scriptedLaunchOpts ++= Seq(
    "-Xmx2048M",
    "-XX:MaxMetaspaceSize=512M",
    s"-Dplugin.version=${version.value}"
  ),
  scriptedBufferLog := false,
  releaseEarlyWith := SonatypePublisher
)
