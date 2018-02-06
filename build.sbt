import Dependencies._

val env = sys.props.getOrElse("env", sys.env.getOrElse("BUILD_ENV", "local"))

lazy val application = project
  .in(file("."))
  .settings(
    name := "Minesweeper",
    inThisBuild(List(
      organization := "com.bonosludos",
      scalaVersion := "2.12.4",
      version      := "0.0.1"
    )),
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= akkaLibraries,
    libraryDependencies ++= logging,
    libraryDependencies ++= slick,

    javaOptions in Universal += s"-Dconfig.resource=application.$env.conf",
    // testOptions in Test += Tests.Argument("-l", "org.scalatest.tags.Slow")
  )
  .enablePlugins(JavaAppPackaging)
