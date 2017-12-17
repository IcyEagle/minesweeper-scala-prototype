package helpers

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

object Client {
  def props(host: String, port: Int, replies: ActorRef) =
    Props(classOf[Client], host, port, replies)

  final object Ready
}

class Client(host: String, port: Int, listener: ActorRef) extends Actor {

  import Tcp._
  import context.system
  import Client._

  IO(Tcp) ! Connect(new InetSocketAddress(host, port))

  def receive: Receive = {
    case CommandFailed(_: Connect) ⇒
      listener ! "connect failed"
      context stop self

    case Connected(remote, local) ⇒
      listener ! Ready
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString ⇒
          connection ! Write(data)
        case CommandFailed(w: Write) ⇒
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) ⇒
          listener ! data
        case "close" ⇒
          connection ! Close
        case _: ConnectionClosed ⇒
          listener ! "connection closed"
          context stop self
      }
  }
}