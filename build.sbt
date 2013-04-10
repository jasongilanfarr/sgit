name := "sgit"

organization := "org.thisamericandream"

version := "0.1"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "net.java.dev.jna" % "jna" % "3.5.2",
  "org.scala-lang" % "scala-reflect" % "2.10.1", // TODO: Use scalaVersion above.
   "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"
)

fork in Test := true

parallelExecution in Test := false
