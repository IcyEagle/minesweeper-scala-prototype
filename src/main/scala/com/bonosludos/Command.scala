package com.bonosludos

trait Command
case class AuthCommand(signature: String) extends Command

case class RegisteredCommandResponse(id: String) extends Command