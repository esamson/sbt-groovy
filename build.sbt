lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    inThisBuild(
      Seq(
        organization := "ph.samson",
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
        scalaVersion := "2.12.13",
        pgpPublicRing := file("./travis/pubring.asc"),
        pgpSecretRing := file("./travis/secring.asc"),
        releaseEarlyWith := SonatypePublisher
      )
    ),
    name := "sbt-groovy",
    scriptedLaunchOpts ++= Seq(
      "-Xmx2048M",
      "-XX:MaxMetaspaceSize=512M",
      s"-Dplugin.version=${version.value}"
    ),
    scriptedBufferLog := false
  )
