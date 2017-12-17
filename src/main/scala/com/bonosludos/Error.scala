package com.bonosludos

import akka.util.ByteString

object Error {
  case class CommandDecodingNotSupported(name: String, data: ByteString) extends Error(s"Command `${name.map(_.toHexString).mkString(" ")}` is not supported. Data `${data.map(_.toHexString).mkString(" ")}`")
  case class CommandDecodingFailed(data: ByteString, command: Class[_], cause: Exception) extends Error(s"${command.getSimpleName} can't be parsed. Reason: ${cause.getClass.getName}. Data: `${data.map(_.toHexString).mkString(" ")}`")
  case class CommandEncodingNotSupported(command: Command) extends Error(s"Command `${command.getClass.getSimpleName}` can't be encoded")
  case class LegacyError(reason: Throwable) extends Error(s"Internal error ${reason.toString}")
}

class Error(message: String) extends RuntimeException(message)