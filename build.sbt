name := "sapper"

organization := "com.gajdulewicz"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "com.chuusai" %% "shapeless" % "2.2.5"
)