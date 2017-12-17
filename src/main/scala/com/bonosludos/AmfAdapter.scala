package com.bonosludos

import java.nio.charset.Charset
import java.nio.{ByteBuffer, ByteOrder}

import akka.util.{ByteString, ByteStringBuilder}
import com.bonosludos.Error.{CommandDecodingFailed, CommandDecodingNotSupported, CommandEncodingNotSupported}

trait AmfUtilities {
  val charset: Charset = Charset.forName("UTF-8")
  implicit val byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN

  def readAMFInt(data: ByteBuffer): Int = {
    var n : Int = 0
    var b : Int = data.get
    var result : Int = 0

    while((b & 0x80) != 0 && n < 3)
    {
      result <<= 7
      result |= (b & 0x7f)
      b = data.get()
      n += 1
    }

    if(n < 3)
    {
      result <<= 7
      result |= b
    }
    else
    {
      result <<= 8
      result |= b & 0x0ff
      if((result & 0x10000000) != 0)
      {
        result |= 0xe0000000
      }
    }

    result
  }

  def readAMFString(data: ByteBuffer): String = {
    val length = readAMFInt(data) >> 1
    readUTFString(data, length)
  }

  def readString(data: ByteBuffer): String = {
    val length = data.getShort()
    readUTFString(data, length)
  }

  def readUTFString(data: ByteBuffer, length: Int): String = {
    val copy = data.slice()
    data.position(data.position() + length)

    charset.decode(copy.limit(copy.position() + length).asInstanceOf[ByteBuffer]).toString
  }

  def writeString(builder: ByteStringBuilder, string: String): ByteStringBuilder = {
    builder.putShort(string.length.toShort)
    builder ++= ByteString(string)
  }

  def writeAMFString(data: ByteStringBuilder, string: String): ByteStringBuilder = {
    writeAMFInt(data, string.length << 1 | 1)
    writeUTFString(data, string)
  }

  def writeAMFInt(builder: ByteStringBuilder, value: Int): ByteStringBuilder = {
    if(value < 0) {
      builder.putByte((0x80 | ((value >> 22) & 0xff)).toByte)
      builder.putByte((0x80 | ((value >> 15) & 0x7f)).toByte)
      builder.putByte((0x80 | ((value >> 8) & 0x7f)).toByte)
      builder.putByte((value & 0xff).toByte)
    }
    else if(value <= 0x7f) {
      builder.putByte(value.toByte)
    }
    else if(value <= 0x3fff) {
      builder.putByte((0x80 | (value >> 7) & 0x7f).toByte)
      builder.putByte((value & 0x7f).toByte)
    }
    else if(value <= 0x1fffff) {
      builder.putByte((0x80 | (value >> 14) & 0x7f).toByte)
      builder.putByte((0x80 | (value >> 7) & 0x7f).toByte)
      builder.putByte((value & 0x7f).toByte)
    } else {
      builder.putByte((0x80 | ((value >> 22) & 0xff)).toByte)
      builder.putByte((0x80 | ((value >> 15) & 0x7f)).toByte)
      builder.putByte((0x80 | ((value >> 8) & 0x7f)).toByte)
      builder.putByte((value & 0xff).toByte)
    }
  }

  def writeUTFString(builder: ByteStringBuilder, string: String): ByteStringBuilder = {
    builder.putBytes(string.getBytes(charset))
  }
}

class AmfAdapter(inputs: List[(Class[_], String, ByteBuffer => Command)], outputs: List[(Class[_], String, Command => ByteString)]) extends Adapter with AmfUtilities {

  private val encoders: Map[Class[_], Command => ByteString] = outputs.map((tuple) => {tuple._1 -> tuple._3}).toMap
  private val decoders: Map[String, ByteBuffer => Command] = inputs.map((tuple) => {tuple._2 -> tuple._3}).toMap
  private val names: Map[Class[_], String] = outputs.map((tuple) => {tuple._1 -> tuple._2}).toMap
  private val commandClasses: Map[String, Class[_]] = inputs.map((tuple) => {tuple._2 -> tuple._1}).toMap

//  private def readHeader(data: ByteBuffer): Unit = {
//    val objectType = data.getChar()
//    if (objectType != 0xA) throw BadCommandFormat(objectType + data.toString)
//
//    val flags = data.getChar()
//    if (flags != 0x7) throw BadCommandFormat(objectType + flags + data.toString)
//  }

//  private def writeHeader(data: ByteBuffer) = {
//    data.put(0xA.toByte) // Object/Class is described ahead
//    data.put(0x7.toByte) // AMF specific byte (not a reference, typed class, IExternalizable)
//  }

  override def decode(data: ByteString): Command = {
    val buffer = data.toByteBuffer
    val command = readAMFString(buffer)
    val decoder = decoders.getOrElse(command, throw CommandDecodingNotSupported(command, data))

    try {
      decoder(buffer)
    } catch {
      case e: Exception => throw CommandDecodingFailed(data, commandClasses(command), e)
    }
  }

  override def encode[T <: Command](command: T): ByteString = {
    val builder = ByteString.newBuilder

    writeAMFString(builder, names.getOrElse(command.getClass, throw CommandEncodingNotSupported(command)))

    val encoder = encoders.getOrElse(command.getClass, throw CommandEncodingNotSupported(command))
    builder ++= encoder(command)

    builder.result()
  }
}
