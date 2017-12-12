sbt-groovy
==========

An sbt plugin for groovy forked from
[fupelaqu/sbt-groovy](https://github.com/fupelaqu/sbt-groovy)

## Requirements

* [SBT 0.13+](http://www.scala-sbt.org/)


## Quick start

Add plugin to *project/plugins.sbt*:

```scala
addSbtPlugin("ph.samson" % "sbt-groovy" % "0.2.0")
```

For *.sbt* build definitions, inject the plugin settings in *build.sbt*:

```scala
seq(groovy.settings :_*)

seq(testGroovy.settings :_*)
```

For *.scala* build definitions, inject the plugin settings in *Build.scala*:

```scala
Project(..., settings = Project.defaultSettings ++ ph.samson.sbt.groovy.GroovyPlugin.groovy.settings ++ ph.samson.sbt.groovy.GroovyPlugin.testGroovy.settings)
```

## Configuration

Plugin keys are located in `ph.samson.sbt.groovy.Keys`

### Groovy sources

```scala
groovySource in Compile := (sourceDirectory in Compile).value / "groovy"

groovySource in Test := (sourceDirectory in Test).value / "groovy"
```

