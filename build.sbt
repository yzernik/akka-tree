import com.typesafe.sbt.SbtAspectj._

name := "actor-tree"

organization := "nworks"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  Seq(
    "io.spray"            %%  "spray-can"     % "1.3.1",
    "io.spray"            %%  "spray-routing" % "1.3.1",
    "com.typesafe.akka"   %% "akka-http-experimental" % "0.11",
    "com.typesafe.play"   %% "play-json"              % "2.3.0",
    "com.typesafe.akka"   %% "akka-actor"             % "2.3.7",
    "org.aspectj"         %  "aspectjweaver"          % "1.7.2",
    "org.aspectj"         %  "aspectjrt"              % "1.7.2"
  )
}

// The Typesafe repository
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)


aspectjSettings

Revolver.settings


javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

fork in run := true

connectInput in run := true

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .aggregate(scalajs)

lazy val scalajs = project in file("scalajs")
