package com.bonosludos

import akka.util.ByteString

trait Adapter {
  def decode(data: ByteString): Command
  def encode[T <: Command](command: T): ByteString
}
