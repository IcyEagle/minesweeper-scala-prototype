package com.bonosludos.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import helpers.Client
import helpers.Client.Ready
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class ServerSpec extends TestKit(ActorSystem("ServerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Server" must {

    "receive connection on port 9944 on localhost" in {
      system.actorOf(Server.props("localhost", 9944))
      system.actorOf(Client.props("localhost", 9944, self))

      expectMsg(Ready)
    }

    "die if socket can't be opened" in {
      val server = system.actorOf(Server.props("localhost", 80)) // 80 - protected port

      watch(server)

      expectTerminated(server)
    }
  }
}