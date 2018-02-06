import sbt._

object Dependencies {
  val akkaVersion = "2.5.8"
  val slickVersion = "3.2.0"
  val postgresVersion = "42.1.4"
  val slickPgVersion = "0.15.4"
  val json4sVersion = "3.6.0-M2"
  val forkliftVersion = "0.3.1"

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

  lazy val slick = Seq(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "org.postgresql" % "postgresql" % postgresVersion,
    "com.github.tminglei" %% "slick-pg" % slickPgVersion,
    "com.github.tminglei" %% "slick-pg_json4s" % slickPgVersion,
    "org.json4s" %% "json4s-native" % json4sVersion,
    "com.liyaos" %% "scala-forklift-slick" % forkliftVersion,
  )
}
