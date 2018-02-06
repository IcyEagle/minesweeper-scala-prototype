package com.bonosludos.actors

import akka.actor.{Actor, ActorRef}

class Commander(iosValidator: ActorRef) extends Actor with ActorCustomLogging {

  override def receive: Receive = {
    null
//    case IosAuth(playerId, publicKeyUrl, timestamp, salt64, signature64, bundleId) =>
//      (iosValidator ? Validate(playerId, publicKeyUrl, timestamp, salt64, signature64, bundleId)) map {
//        case ValidationSuccess =>
//          sender() ! IosAuthSuccessResponse(123)
//        case ValidationFailure =>
//          sender() ! IosAuthFailedResponse
//      }
  }
}
