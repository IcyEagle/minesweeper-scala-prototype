package com.bonosludos.actors

import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.Timeout
import com.bonosludos.Event.{ConnectionClosed, ConnectionEstablished}
import com.bonosludos._

import scala.concurrent.Future
import scala.concurrent.duration._

object ConnectionHandler {

  type Parser = (ByteBuffer) => Command
  type Code = String
  type Parsers = Map[Code, Parser]

  def props(parsers: Parsers): Props = Props(classOf[ConnectionHandler], parsers)
  def props(adapter: Adapter): Props = {
    Props(classOf[ConnectionHandler], adapter, null)
  }
}

class ConnectionHandler(adapter: Adapter, commander: ActorRef) extends Actor with ActorCustomLogging {

  import Tcp._
  import akka.pattern.ask
  import context.dispatcher
  import com.bonosludos.Command
  implicit private val timeout: Timeout = Timeout(5 seconds)

  def receive: Receive = {
    case Received(data) =>
      debug(ConnectionEstablished)

      val source = sender()

      Future(adapter.decode(data))
        .flatMap(command => commander ? command).mapTo[Command]
        .map(command => adapter.encode(command))
        .map(data => source ! Write(data))
        .recover { case exception => error(exception) }
    case PeerClosed =>
      debug(ConnectionClosed)
      context stop self
  }

}

