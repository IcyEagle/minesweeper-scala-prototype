package com.bonosludos.actors

import java.net.URL
import java.nio.ByteBuffer
import java.security.Signature
import java.security.cert.{Certificate, CertificateFactory}
import java.util.Base64

import akka.actor.{Actor, Props}
import com.bonosludos.Error.IosAuthDataCorrupted
import com.bonosludos.Event.{NewIosCertificateDownloaded, UntrustedIosCertificateDetected}
import com.bonosludos.actors.IosCertificateValidator.{Validate, ValidationFailure, ValidationSuccess}

import scala.collection.mutable

object IosCertificateValidator {
  case class Validate(playerId: String, publicKeyUrl: String, timestamp: Long, salt64: String, signature64: String, bundleId: String)
  case object ValidationSuccess
  case object ValidationFailure

  def props(certificates: Map[String, Certificate] = Map.empty): Props = Props(classOf[IosCertificateValidator], certificates)
}

class IosCertificateValidator(predefinedCertificates: Map[String, Certificate]) extends Actor with ActorCustomLogging {

  private val trustedPrefix = "https://static.gc.apple.com/public-key"
  private val certificates = mutable.HashMap(predefinedCertificates.toSeq: _*)
  private val factory = CertificateFactory.getInstance("X.509")
  private val decoder = Base64.getDecoder

  private def getCertificate(url: String): Certificate = {
    certificates.getOrElseUpdate(url, {
      info(NewIosCertificateDownloaded(url)) // add certificate into "resources" asap
      factory.generateCertificate(new URL(url).openStream())
    })
  }

  override def receive: Receive = {
    case Validate(playerId, publicKeyUrl, timestamp, salt64, signature64, bundleId) =>
      try {
        if (publicKeyUrl.startsWith(trustedPrefix)) {
          val certificate = getCertificate(publicKeyUrl)
          val salt = decoder.decode(salt64)
          val signature = decoder.decode(signature64)
          val buffer = ByteBuffer.allocate(100)
            .put(playerId.getBytes())
            .put(bundleId.getBytes())
            .putLong(timestamp)
            .put(salt)
            .flip()
            .asInstanceOf[ByteBuffer]

          val sig = Signature.getInstance("SHA256withRSA")
          sig.initVerify(certificate)
          sig.update(buffer)

          val answer = if (sig.verify(signature)) ValidationSuccess else ValidationFailure
          sender() ! answer
        } else {
          warning(UntrustedIosCertificateDetected(playerId, publicKeyUrl, timestamp, salt64, signature64, bundleId))
          sender() ! ValidationFailure
        }
      } catch {
        case e: Exception =>
          error(IosAuthDataCorrupted(playerId, publicKeyUrl, timestamp, salt64, signature64, bundleId, e))
          sender() ! ValidationFailure
      }
  }
}
