package com.bonosludos

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {

  import actors._

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Universe")

    system.actorOf(Server.props("localhost", 1333), "server")

    Await.ready(system.whenTerminated, Duration.Inf)
  }
}
