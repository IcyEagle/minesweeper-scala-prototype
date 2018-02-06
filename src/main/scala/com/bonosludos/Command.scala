package com.bonosludos

trait Command
case class IosAuth(playerId: String, publicKeyUrl: String, timestamp: Long, salt64: String, signature64: String, bundleId: String) extends Command

case object IosAuthFailedResponse extends Command
case class IosAuthSuccessResponse(id: Int) extends Command