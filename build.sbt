name := "option-id-validation"

version := "0.1"

scalaVersion := "2.12.6"

mainClass := Some("fsm.Main")

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % versions.CatsVersion,

      "com.typesafe.akka" %% "akka-persistence" % versions.AkkaVersion,
      "com.typesafe.akka" %% "akka-stream"      % versions.AkkaVersion,
      "com.typesafe.akka" %% "akka-persistence-query" % versions.AkkaVersion,

      "org.iq80.leveldb" % "leveldb" % "0.7",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

      "com.kailuowang" %% "henkan-optional" % "0.6.1",

      "org.scalatest" %% "scalatest" % versions.ScalaTestVersion % Test
    )
  )

lazy val commonSettings =
  Seq(
    scalacOptions ++= Seq("-Ypartial-unification")
  )

lazy val versions = new {
  val CatsVersion       = "1.1.0"
  val ScalaTestVersion  = "3.0.5"
  val AkkaVersion       = "2.5.13"
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)