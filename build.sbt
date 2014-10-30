organization := "tv.cntt"

name         := "jekytrum"

version      := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"
//scalaVersion := "2.10.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Ywarn-unused", "-Ywarn-unused-import", "-Xlint")

// Xitrum requires Java 7
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s" % "1.3.2"

libraryDependencies += "org.pegdown" % "pegdown" % "1.4.2"

libraryDependencies += "com.ning"    %  "async-http-client" % "1.8.14"


//------------------------------------------------------------------------------

libraryDependencies += "tv.cntt" %% "xitrum" % "3.19-SNAPSHOT"

// Xitrum uses SLF4J, an implementation of SLF4J is needed
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

// For writing condition in logback.xml
libraryDependencies += "org.codehaus.janino" % "janino" % "2.7.5"

libraryDependencies += "org.webjars" % "bootstrap" % "3.2.0"

// Scalate template engine config for Xitrum -----------------------------------

libraryDependencies += "tv.cntt" %% "xitrum-scalate" % "2.3-SNAPSHOT"

// Precompile Scalate templates
seq(scalateSettings:_*)

ScalateKeys.scalateTemplateConfig in Compile := Seq(TemplateConfig(
  baseDirectory.value / "src" / "main" / "scalate",
  Seq(),
  Seq(Binding("helper", "xitrum.Action", true))
))

// xgettext i18n translation key string extractor is a compiler plugin ---------

autoCompilerPlugins := true

addCompilerPlugin("tv.cntt" %% "xgettext" % "1.2")

scalacOptions += "-P:xgettext:xitrum.I18n"

// Put config directory in classpath for easier development --------------------

// For "sbt console"
unmanagedClasspath in Compile <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }

// For "sbt run"
unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") }

// Copy these to target/xitrum when sbt xitrum-package is run
XitrumPackage.copy("config", "public", "script")
