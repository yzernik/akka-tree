import com.typesafe.sbt.SbtAspectj._

name := "actor-tree"

organization := "nworks"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  Seq(
    "com.typesafe.akka"   %% "akka-http-experimental" % "0.11",
    "com.typesafe.play"   %% "play-json"              % "2.3.0",
    "com.typesafe.akka"   %% "akka-actor"             % "2.3.7",
    "org.aspectj"         %  "aspectjweaver"          % "1.8.4",
    "org.aspectj"         %  "aspectjrt"              % "1.8.4",
    "org.apache.curator"  % "curator-test"            % "2.7.0", //gives im-memory zookeeper
    "org.apache.kafka"    %% "kafka"                  % "0.8.2-beta"
  )
}

libraryDependencies ++= {
  Seq(
    "junit" % "junit" % "4.10" % "test"
    //, "org.scalatest" %% "scalatest" % "2.2.2" % "test"
  )
}

// The Typesafe repository
resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

aspectjSettings

javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

javacOptions += "-g"

fork in run := true

connectInput in run := true

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .dependsOn(scalajs)
  .aggregate(scalajs)

lazy val scalajs = project in file("scalajs")
