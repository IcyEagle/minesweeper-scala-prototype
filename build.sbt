import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.bonosludos",
      scalaVersion := "2.12.4",
      version      := "0.0.1"
    )),
    name := "Minesweeper",
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= akkaLibraries,
    libraryDependencies ++= logging
  )
