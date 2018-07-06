name := "option-id-validation"

version := "0.1"

scalaVersion := "2.12.6"

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % versions.CatsVersion,
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
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)