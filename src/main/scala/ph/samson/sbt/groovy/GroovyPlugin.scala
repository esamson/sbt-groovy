package ph.samson.sbt.groovy

import java.io.File

import sbt.Keys._
import sbt._
import Path.relativeTo

object GroovyPlugin extends AutoPlugin {
  self =>

  override lazy val projectSettings: Seq[Setting[_]] =
    groovy.settings ++ testGroovy.settings

  object autoImport {
    lazy val groovyVersion = settingKey[String]("groovy version")
    lazy val groovySource = settingKey[File]("Default groovy source directory")
    lazy val groovyc = taskKey[Seq[File]]("Compile Groovy sources")
    lazy val Groovy = (config("groovy") extend Compile).hide
    lazy val GroovyTest = (config("test-groovy") extend Test).hide
    lazy val GroovyIT = (config("it-groovy") extend IntegrationTest).hide
  }

  import autoImport._

  def defaultSettings(config: Configuration) = Seq(
    groovyVersion := "2.5.4",
    libraryDependencies ++= Seq[ModuleID](
      "org.codehaus.groovy" % "groovy-all" % groovyVersion.value % config.name,
      "org.apache.ant" % "ant" % "1.10.5" % config.name
    ),
    groovyc / managedClasspath := Classpaths
      .managedJars(config, (groovyc / classpathTypes).value, update.value)
  )

  // to avoid namespace clashes, use a nested object
  object groovy {

    lazy val groovycFilter: ScopeFilter = ScopeFilter(
      inDependencies(ThisProject, transitive = true, includeRoot = false),
      inConfigurations(Groovy),
      inTasks(groovyc))

    lazy val compileFilter: ScopeFilter = ScopeFilter(
      inDependencies(ThisProject, transitive = true, includeRoot = false),
      inConfigurations(Compile))

    lazy val settings: Seq[Setting[_]] = Seq(ivyConfigurations += Groovy) ++
      defaultSettings(Groovy) ++
      Seq(
        Compile / groovySource := (Compile / sourceDirectory).value / "groovy",
        Compile / unmanagedSourceDirectories += {
          (Compile / groovySource).value
        },
        Groovy / groovyc / classDirectory := (Compile / crossTarget).value / "groovy-classes",
        groovyc / managedClasspath := Classpaths
          .managedJars(Groovy, (groovyc / classpathTypes).value, update.value),
        Compile / groovyc := {
          val sourceDirectory: File = (Compile / groovySource).value
          val nb = (sourceDirectory ** "*.groovy").get.size

          lazy val managedClasspathInGroovyC =
            (groovyc / managedClasspath).value
          lazy val managedClasspathInCompile =
            (Compile / managedClasspath).value
          lazy val sourceManagedInCompile = (Compile / sourceManaged).value
          lazy val classDirectoryInGroovy =
            (Groovy / groovyc / classDirectory).value
          lazy val resourceManagedInCompile = (Compile / resourceManaged).value
          lazy val classDirectoryInCompile = (Compile / classDirectory).value

          lazy val classDirectoryAllCompiler =
            classDirectory.all(compileFilter).value
          lazy val classDirectoryAllGroovy =
            classDirectory.all(groovycFilter).value

          lazy val s: TaskStreams = streams.value
          s.log.debug(s"nb: $nb")

          if (nb > 0) {

            s.log.info(
              s"Start Compiling Groovy sources : ${sourceDirectory.getAbsolutePath} ")

            val classDirectories: Seq[File] = classDirectoryAllCompiler ++
              classDirectoryAllGroovy ++
              Seq(classDirectoryInCompile)

            val classpath: Seq[File] = managedClasspathInGroovyC.files ++
              classDirectories ++
              managedClasspathInCompile.files
            s.log.debug(classpath.mkString(";"))
            val stubDirectory: File = sourceManagedInCompile
            val destinationDirectory: File = classDirectoryInGroovy

            new GroovyC(classpath,
                        sourceDirectory,
                        stubDirectory,
                        destinationDirectory).compile()

            ((destinationDirectory ** "*.class").get pair relativeTo(
              destinationDirectory)).map {
              case (k, v) =>
                IO.copyFile(k,
                            resourceManagedInCompile / v,
                            preserveLastModified = true)
                resourceManagedInCompile / v
            }
          } else {
            Seq.empty
          }
        },
        Compile / resourceGenerators += Compile / groovyc,
        Compile / groovyc := ((Compile / groovyc) dependsOn (Compile / compile)).value
      )
  }

  object testGroovy {

    lazy val groovycTestFilter: ScopeFilter = ScopeFilter(
      inDependencies(ThisProject, transitive = true, includeRoot = false),
      inConfigurations(Groovy),
      inTasks(groovyc))

    lazy val compileTestFilter: ScopeFilter = ScopeFilter(
      inDependencies(ThisProject, transitive = true, includeRoot = false),
      inConfigurations(Test))

    lazy val settings: Seq[Setting[_]] = Seq(ivyConfigurations += GroovyTest) ++
      inConfig(GroovyTest)(
        Defaults.testTasks ++ Seq(
          definedTests := (Test / definedTests).value,
          definedTestNames := (Test / definedTestNames).value,
          fullClasspath := (Test / fullClasspath).value)) ++
      defaultSettings(GroovyTest) ++
      Seq(
        Test / groovySource := (Test / sourceDirectory).value / "groovy",
        Test / unmanagedSourceDirectories += {
          (Test / groovySource).value
        },
        GroovyTest / groovyc / classDirectory := (Test / crossTarget).value / "groovy-test-classes",
        groovyc / managedClasspath := Classpaths.managedJars(
          GroovyTest,
          (groovyc / classpathTypes).value,
          update.value),
        Test / groovyc := {
          val sourceDirectory: File = (Test / groovySource).value
          val nb = (sourceDirectory ** "*.groovy").get.size

          lazy val managedClasspathInGroovyC =
            (groovyc / managedClasspath).value
          lazy val managedClasspathInTest = (Test / managedClasspath).value
          lazy val sourceManagedInTest = (Test / sourceManaged).value
          lazy val classDirectoryInGroovyTest =
            (GroovyTest / groovyc / classDirectory).value
          lazy val resourceManagedInTest = (Test / resourceManaged).value

          lazy val classDirectoryAllCompile =
            classDirectory.all(groovy.compileFilter).value
          lazy val classDirectoryAllGroovyC =
            classDirectory.all(groovy.groovycFilter).value
          lazy val classDirectoryAllCompileTest =
            classDirectory.all(compileTestFilter).value
          lazy val classDirectoryAllGroovyCTest =
            classDirectory.all(groovycTestFilter).value
          lazy val classDirectoryInCompile = (Compile / classDirectory).value
          lazy val classDirectoryInGroovy =
            (Groovy / groovyc / classDirectory).value

          lazy val s: TaskStreams = streams.value

          if (nb > 0) {

            s.log.info(
              s"Start Compiling Test Groovy sources : ${sourceDirectory.getAbsolutePath} ")

            val classDirectories: Seq[File] = classDirectoryAllCompile ++
              classDirectoryAllGroovyC ++ classDirectoryAllCompileTest ++
              classDirectoryAllGroovyCTest ++
              Seq(classDirectoryInCompile, classDirectoryInGroovy)

            val classpath
              : Seq[File] = managedClasspathInGroovyC.files ++ classDirectories ++ managedClasspathInTest.files
            s.log.debug(classpath.mkString(";"))

            val stubDirectory: File = sourceManagedInTest

            val destinationDirectory: File = classDirectoryInGroovyTest

            new GroovyC(classpath,
                        sourceDirectory,
                        stubDirectory,
                        destinationDirectory).compile()

            ((destinationDirectory ** "*.class").get pair relativeTo(
              destinationDirectory)).map {
              case (k, v) =>
                IO.copyFile(k,
                            resourceManagedInTest / v,
                            preserveLastModified = true)
                resourceManagedInTest / v
            }
          } else {
            Seq.empty
          }
        },
        Test / resourceGenerators += Test / groovyc,
        Test / groovyc := ((Test / groovyc) dependsOn (Test / compile)).value
      )
  }

}
