import sbt._

object Dependencies {
  val akkaVersion = "2.5.8"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4"

  lazy val akkaLibraries = Seq(
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  )
}
