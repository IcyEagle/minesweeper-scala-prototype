package com.bonosludos

import java.nio.ByteBuffer

import akka.util.ByteString
import com.bonosludos.Error.{CommandDecodingFailed, CommandDecodingNotSupported, CommandEncodingNotSupported}
import org.scalatest.{Matchers, WordSpec}

class AmfAdapterSpec extends WordSpec with Matchers with AmfUtilities {

  case object SimpleCommand extends Command
  case class CompoundCommand(value: Int, sentence: String) extends Command
  case object UnsupportedCommand extends Command
  val inputs: List[(Class[_], String, ByteBuffer => Command)] = List(
    (SimpleCommand.getClass, "S", (_: ByteBuffer) => SimpleCommand),
    (CompoundCommand(0, "abc").getClass, "C", (data: ByteBuffer) => {
      val value = data.getInt
      val string = readString(data)
      CompoundCommand(value, string)
    })
  )

  val outputs: List[(Class[_], String, Command => ByteString)] = List(
    (SimpleCommand.getClass, "S", (_: Command) => ByteString()),
    (CompoundCommand(0, "abc").getClass, "C", (command: Command) => {
      val CompoundCommand(value, sentence) = command.asInstanceOf[CompoundCommand]
      writeString(ByteString.newBuilder.putInt(value), sentence).result()
    })
  )
  val adapter = new AmfAdapter(inputs, outputs)

  implicit val toByteArray: Array[Int] => Array[Byte] = (intArray: Array[Int]) => intArray.map(_.toByte)

  "An adapter" can {

    "encode" should {

      "return encoded Simple command" in {
        adapter.encode(SimpleCommand) shouldEqual ByteString.fromArray(Array(3, 83))
      }

      "return encoded Compound command" in {
        adapter.encode(CompoundCommand(42, "foo")) shouldEqual ByteString.fromArray(Array(3, 67, 0, 0, 0, 42, 0, 3, 102, 111, 111))
      }

      "produce CommandEncodingNotSupported when the command is not supported" in {
        intercept[CommandEncodingNotSupported] {
          adapter.encode(UnsupportedCommand)
        }
      }

    }

    "decode" should {

      "return Simple command instance" in {
        adapter.decode(ByteString.fromArray(Array(3, 83))) shouldEqual SimpleCommand
      }

      "return Compound command instance" in {
        adapter.decode(ByteString.fromArray(Array(3, 67, 0, 0, 0, 42, 0, 3, 102, 111, 111))) shouldEqual CompoundCommand(42, "foo")
      }

      "produce CommandEncodingNotSupported when the command is not supported" in {
        intercept[CommandDecodingNotSupported] {
          adapter.decode(ByteString.fromArray(Array(3, 22)))
        }
      }

      "produce Exception when the command can't be parsed" in {
        intercept[CommandDecodingFailed] {
          adapter.decode(ByteString.fromArray(Array(3, 67, 12, 12, 12)))
        }
      }

    }
  }
}
