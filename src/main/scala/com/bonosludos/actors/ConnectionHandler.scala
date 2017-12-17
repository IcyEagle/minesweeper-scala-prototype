package com.bonosludos.actors

import java.nio.ByteBuffer

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.{ByteString, Timeout}
import com.bonosludos.Event.{ConnectionClosed, ConnectionEstablished}
import com.bonosludos.{Adapter, Command}

import scala.concurrent.Future

object ConnectionHandler {
  private lazy val defaultCommands = Map()

  type Parser = (ByteBuffer) => Command
  type Code = String
  type Parsers = Map[Code, Parser]

  def props(parsers: Parsers): Props = Props(classOf[ConnectionHandler], parsers)
  def props(): Props = Props(classOf[ConnectionHandler], defaultCommands)
}

class ConnectionHandler(adapter: Adapter, commander: ActorRef) extends Actor with ActorCustomLogging {

  import Tcp._
  import com.bonosludos.Command
  import context.dispatcher
  implicit private val timeout: Timeout = Timeout(5 seconds)

  def receive: Receive = {
    case Received(data) =>
      debug(ConnectionEstablished)

      Future(adapter.decode(data))
        .flatMap(command => commander ? command)
        .map(response => response.asInstanceOf[Command])
        .map(command => adapter.encode(command))
        .map(data => sender() ! Write(data))
        .recover { case exception => error(exception) }
    case PeerClosed => debug(ConnectionClosed)

      context stop self
  }

}

