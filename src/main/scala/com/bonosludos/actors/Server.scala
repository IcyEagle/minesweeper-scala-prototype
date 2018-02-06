package com.bonosludos.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import com.bonosludos.AmfAdapter
import com.bonosludos.Event.{ConnectionEstablished, ConnectionServerFailed, ConnectionServerStarted}

object Server {

  def props(host: String, port: Int): Props = Props(classOf[Server], host, port)
}

class Server(host: String, port: Int) extends Actor with ActorCustomLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  override def receive: Receive = {
    case b @ Bound(_) ⇒
      info(ConnectionServerStarted(host, port))
      context.parent ! b

    case CommandFailed(_: Bind) =>
      error(ConnectionServerFailed(host, port))
      context stop self

    case c @ Connected(_, _) =>
      debug(ConnectionEstablished)
      val adapter = AmfAdapter()
      val handler = context.actorOf(ConnectionHandler.props(adapter))
      sender() ! Register(handler)
  }
}
