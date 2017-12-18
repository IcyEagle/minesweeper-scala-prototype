package com.bonosludos

trait Command
case class IosAuthCommand(playerId: String, publicKeyUrl: String, timestamp: Int, salt64: String, signature64: String, bundleId: String) extends Command

case class RegisteredCommandResponse(id: String) extends Command