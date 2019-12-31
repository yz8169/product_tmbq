import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "product_tmbq"

version := "0.1"

scalaVersion := "2.13.1"

lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client),
  name := "product_tmbq",
  version := "1.0",
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.3",
    guice,
    specs2 % Test,
    ws,
    ehcache,
    "com.typesafe.play" %% "play-slick" % "4.0.2",
    "com.typesafe.slick" %% "slick-codegen" % "3.3.2",
    "mysql" % "mysql-connector-java" % "5.1.25",
    "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.1",
    "commons-io" % "commons-io" % "2.5",
    "org.apache.poi" % "poi-ooxml" % "3.15",
    "org.apache.commons" % "commons-math3" % "3.6.1",
    "com.aliyun" % "aliyun-java-sdk-core" % "3.7.1",
    "com.aliyun" % "aliyun-java-sdk-dysmsapi" % "1.1.0",
    "com.typesafe.play" %% "play-joda-forms" % "2.7.3",
    "org.apache.xmlgraphics" % "batik-codec" % "1.10",
    "org.zeroturnaround" % "zt-zip" % "1.11",
    "com.github.tototoshi" %% "scala-csv" % "1.3.6",
    "junit" % "junit" % "4.12" % Test,
    "org.apache.pdfbox" % "pdfbox" % "2.0.8",
    "org.apache.pdfbox" % "pdfbox-tools" % "2.0.8",
    "org.scalaz" %% "scalaz-core" % "7.2.29",
    "org.scalaz" %% "scalaz-effect" % "7.2.29",
    "com.h2database" % "h2" % "1.4.199",
    "javax.xml.bind" % "jaxb-api" % "2.3.0",
    "org.apache.commons" % "commons-lang3" % "3.6",
    "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0",
    "org.typelevel" %% "cats-core" % "2.0.0",
    "javax.mail" % "mail" % "1.4.7",

  )
).enablePlugins(PlayScala).dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := false,
  resolvers += "jitpack" at "https://jitpack.io",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.7",
    "com.lihaoyi" %%% "scalatags" % "0.7.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.5"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.13.1",
)

onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }