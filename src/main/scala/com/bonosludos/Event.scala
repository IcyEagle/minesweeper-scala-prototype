package com.bonosludos

object Event {
  case class ExceptionRaised(error: Throwable) extends Event(s"Exception: ${error.toString}")
  case class ClientCommandReceived(command: Command) extends Event("Client command received")
  case object ConnectionEstablished extends Event("Connection established")
  case object ConnectionClosed extends Event("Connection closed")
  case class ConnectionServerStarted(host: String, port: Int) extends Event("Connection server started")
  case class ConnectionServerFailed(host: String, port: Int) extends Event("Connection server can not be started")
}

abstract class Event(message: String) {
  override def toString: String = message
}