package org.softnetwork.sbt.plugins

import sbt._
import Keys._
import java.io.File
import Path.relativeTo

object GroovyPlugin extends AutoPlugin {
  self =>

  override lazy val projectSettings = groovy.settings ++ testGroovy.settings

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
    groovyVersion := "2.1.8",
    libraryDependencies ++= Seq[ModuleID](
      "org.codehaus.groovy" % "groovy-all" % groovyVersion.value % config.name,
      "org.apache.ant" % "ant" % "1.8.4" % config.name
    ),
    /*managedClasspath in groovyc := (classpathTypes in groovyc, update) map { (ct, report) =>
      Classpaths.managedJars(config, ct, report)
    },*/
    managedClasspath in groovyc := Classpaths.managedJars(config, (classpathTypes in groovyc).value, update.value)
  )

  // to avoid namespace clashes, use a nested object
  object groovy {

    lazy val groovycFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Groovy), inTasks(groovyc))

    lazy val compileFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Compile))

    lazy val settings = Seq(ivyConfigurations += Groovy) ++ defaultSettings(Groovy) ++ Seq(
      groovySource in Compile := (sourceDirectory in Compile).value / "groovy",
      unmanagedSourceDirectories in Compile += {
        (groovySource in Compile).value
      },
      classDirectory in(Groovy, groovyc) := (crossTarget in Compile).value / "groovy-classes",
      /*managedClasspath in groovyc := (classpathTypes in groovyc, update) map { (ct, report) =>
          Classpaths.managedJars(Groovy, ct, report)
      },*/
      managedClasspath in groovyc := Classpaths.managedJars(Groovy, (classpathTypes in groovyc).value, update.value),
      groovyc in Compile := {
        val sourceDirectory: File = (groovySource in Compile).value
        val nb = (sourceDirectory ** "*.groovy").get.size

        lazy val managedClasspathInGroovyC = (managedClasspath in groovyc).value
        lazy val managedClasspathInCompile = (managedClasspath in Compile).value
        lazy val sourceManagedInCompile = (sourceManaged in Compile).value
        lazy val classDirectoryInGroovy = (classDirectory in(Groovy, groovyc)).value
        lazy val resourceManagedInCompile = (resourceManaged in Compile).value
        lazy val classDirectoryInCompile = (classDirectory in Compile).value

        lazy val classDirectoryAllCompiler = classDirectory.all(compileFilter).value
        lazy val classDirectoryAllGroovy = classDirectory.all(groovycFilter).value

        lazy val s: TaskStreams = streams.value

        if (nb > 0) {

          s.log.info(s"Start Compiling Groovy sources : ${sourceDirectory.getAbsolutePath} ")

          val classDirectories: Seq[File] = classDirectoryAllCompiler ++
            classDirectoryAllGroovy ++
            Seq(classDirectoryInCompile)

          //val classpath: Seq[File] = (managedClasspath in groovyc).value.files ++ classDirectories ++ (managedClasspath in Compile).value.files
          val classpath: Seq[File] = managedClasspathInGroovyC.files ++ classDirectories ++ (managedClasspathInCompile).files
          s.log.debug(classpath.mkString(";"))
          val stubDirectory: File = (sourceManagedInCompile)
          val destinationDirectory: File = (classDirectoryInGroovy)

          new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()

          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map { case (k, v) =>
            IO.copyFile(k, (resourceManagedInCompile) / v, preserveLastModified = true)
            (resourceManagedInCompile) / v
          }
        }
        else {
          Seq.empty
        }
      },
      resourceGenerators in Compile += groovyc in Compile,
      groovyc in Compile := ((groovyc in Compile) dependsOn (compile in Compile)).value
    )
  }

  object testGroovy {

    lazy val groovycTestFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Groovy), inTasks(groovyc))

    lazy val compileTestFilter: ScopeFilter = ScopeFilter(inDependencies(ThisProject, transitive = true, includeRoot = false), inConfigurations(Test))

    lazy val settings = Seq(ivyConfigurations += GroovyTest) ++ inConfig(GroovyTest)(Defaults.testTasks ++ Seq(
      definedTests := (definedTests in Test).value,
      definedTestNames := (definedTestNames in Test).value,
      fullClasspath := (fullClasspath in Test).value)) ++ defaultSettings(GroovyTest) ++ Seq(

      groovySource in Test := (sourceDirectory in Test).value / "groovy",
      unmanagedSourceDirectories in Test += {
        (groovySource in Test).value
      },
      classDirectory in(GroovyTest, groovyc) := (crossTarget in Test).value / "groovy-test-classes",
      /*managedClasspath in groovyc := (classpathTypes in groovyc, update) map { (ct, report) =>
        Classpaths.managedJars(GroovyTest, ct, report)
      },*/
      managedClasspath in groovyc := Classpaths.managedJars(GroovyTest, (classpathTypes in groovyc).value, update.value),
      groovyc in Test := {
        val sourceDirectory: File = (groovySource in Test).value
        val nb = (sourceDirectory ** "*.groovy").get.size

        lazy val managedClasspathInGroovyC = (managedClasspath in groovyc).value
        lazy val managedClasspathInTest = (managedClasspath in Test).value
        lazy val sourceManagedInTest = (sourceManaged in Test).value
        lazy val classDirectoryInGroovyTest = (classDirectory in(GroovyTest, groovyc)).value
        lazy val resourceManagedInTest = (resourceManaged in Test).value

        lazy val classDirectoryAllCompile = classDirectory.all(groovy.compileFilter).value
        lazy val classDirectoryAllGroovyC = classDirectory.all(groovy.groovycFilter).value
        lazy val classDirectoryAllCompileTest = classDirectory.all(compileTestFilter).value
        lazy val classDirectoryAllGroovyCTest = classDirectory.all(groovycTestFilter).value
        lazy val classDirectoryInCompile = (classDirectory in Compile).value
        lazy val classDirectoryInGroovy = (classDirectory in(Groovy, groovyc)).value

        lazy val s: TaskStreams = streams.value

        if (nb > 0) {

          s.log.info(s"Start Compiling Test Groovy sources : ${sourceDirectory.getAbsolutePath} ")

          val classDirectories: Seq[File] = classDirectoryAllCompile ++
            classDirectoryAllGroovyC ++ classDirectoryAllCompileTest ++
            classDirectoryAllGroovyCTest ++
            Seq(classDirectoryInCompile, classDirectoryInGroovy)

          val classpath: Seq[File] = managedClasspathInGroovyC.files ++ classDirectories ++ managedClasspathInTest.files
          s.log.debug(classpath.mkString(";"))

          val stubDirectory: File = sourceManagedInTest

          val destinationDirectory: File = classDirectoryInGroovyTest

          new GroovyC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile()

          ((destinationDirectory ** "*.class").get pair relativeTo(destinationDirectory)).map { case (k, v) =>
            IO.copyFile(k, resourceManagedInTest / v, preserveLastModified = true)
            resourceManagedInTest / v
          }
        }
        else {
          Seq.empty
        }
      },
      resourceGenerators in Test += groovyc in Test,
      groovyc in Test := ((groovyc in Test) dependsOn (compile in Test)).value
    )
  }

}
