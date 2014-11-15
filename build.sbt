import com.typesafe.sbt.SbtAspectj._

name := "actor-tree"

organization := "nworks"

scalaVersion := "2.11.1"

libraryDependencies ++= {
  val akkaV = "2.3.4"
  val sprayV = "1.3.1"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "com.typesafe.play"   %%  "play-json"     % "2.3.0",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "org.aspectj"         % "aspectjweaver"   % "1.7.2",
    "org.aspectj"         % "aspectjrt"       % "1.7.2",
    "org.specs2"          %%  "specs2"        % "2.3.13" % "test"
  )
}

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

aspectjSettings

Revolver.settings


javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

fork in run := true

connectInput in run := true

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .aggregate(scalajs)

lazy val scalajs = project in file("scalajs")
