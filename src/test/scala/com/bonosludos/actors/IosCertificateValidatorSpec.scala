package com.bonosludos.actors

import java.security.cert.CertificateFactory

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.bonosludos.actors.IosCertificateValidator.{Validate, ValidationFailure, ValidationSuccess}
import org.scalatest.tagobjects.Slow
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class IosCertificateValidatorSpec extends TestKit(ActorSystem("ServerSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Validator" must {

    "accept data with correct signature" in {
      val certificate = CertificateFactory.getInstance("X.509").generateCertificate(getClass.getResourceAsStream("/ios_certificates/gc-prod-3.cer"))
      val server = system.actorOf(IosCertificateValidator.props(Map("https://static.gc.apple.com/public-key/gc-prod-3.cer" -> certificate)))

      server ! Validate("G:286454868", "https://static.gc.apple.com/public-key/gc-prod-3.cer", 1510663544537L, "h6dP7g==", "NbtFOUOy2kksjQBpH3A8Wldx087o3kRGwdH//RP419bVGaoaFb5StbelKZX58ekXL8rFQZUc+bJLfk4edR/lA1uYKVYew9fIWq/Eoda646tGUX9cANQ3OzRil+yeyioD6JPtPq56/eThG+BrjAs1lhknqLcTAeXLzo2QObguBSdSCioPHVp97+wNu3+4J21cWJ40sW2VSSj9VZighqsDCBo7pPmz0JZD8Vma08qmErmriK6G3RhDRM2uxCiPBW32k+/U6hSmQ8RmPOF6cxQBcRWnh0DSRDzvQ7hTy4tyqti4V00PRKCMojZTHBo6RaG0b2k6nnfaaQAFh/7iboFctQ==", "com.griffgriffgames.minesweeper")
      expectMsg(ValidationSuccess)
    }

    "accept data with unknown certificate" taggedAs Slow in {
      val server = system.actorOf(IosCertificateValidator.props())

      server ! Validate("G:286454868", "https://static.gc.apple.com/public-key/gc-prod-3.cer", 1510663544537L, "h6dP7g==", "NbtFOUOy2kksjQBpH3A8Wldx087o3kRGwdH//RP419bVGaoaFb5StbelKZX58ekXL8rFQZUc+bJLfk4edR/lA1uYKVYew9fIWq/Eoda646tGUX9cANQ3OzRil+yeyioD6JPtPq56/eThG+BrjAs1lhknqLcTAeXLzo2QObguBSdSCioPHVp97+wNu3+4J21cWJ40sW2VSSj9VZighqsDCBo7pPmz0JZD8Vma08qmErmriK6G3RhDRM2uxCiPBW32k+/U6hSmQ8RmPOF6cxQBcRWnh0DSRDzvQ7hTy4tyqti4V00PRKCMojZTHBo6RaG0b2k6nnfaaQAFh/7iboFctQ==", "com.griffgriffgames.minesweeper")
      expectMsg(ValidationSuccess)
    }

    "reject data with certificate from malformed URL" in {
      val server = system.actorOf(IosCertificateValidator.props())

      server ! Validate("G:286454868", "https://my-own-site.com/public-key/gc-prod-3.cer", 1510663544537L, "h6dP7g==", "NbtFOUOy2kksjQBpH3A8Wldx087o3kRGwdH//RP419bVGaoaFb5StbelKZX58ekXL8rFQZUc+bJLfk4edR/lA1uYKVYew9fIWq/Eoda646tGUX9cANQ3OzRil+yeyioD6JPtPq56/eThG+BrjAs1lhknqLcTAeXLzo2QObguBSdSCioPHVp97+wNu3+4J21cWJ40sW2VSSj9VZighqsDCBo7pPmz0JZD8Vma08qmErmriK6G3RhDRM2uxCiPBW32k+/U6hSmQ8RmPOF6cxQBcRWnh0DSRDzvQ7hTy4tyqti4V00PRKCMojZTHBo6RaG0b2k6nnfaaQAFh/7iboFctQ==", "com.griffgriffgames.minesweeper")
      expectMsg(ValidationFailure)
    }

    "reject data with incorrect signature" in {
      val certificate = CertificateFactory.getInstance("X.509").generateCertificate(getClass.getResourceAsStream("/ios_certificates/gc-prod-3.cer"))
      val server = system.actorOf(IosCertificateValidator.props(Map("https://static.gc.apple.com/public-key/gc-prod-3.cer" -> certificate)))

      server ! Validate("G:286454868", "https://static.gc.apple.com/public-key/gc-prod-3.cer", 1510663544537L, "h6dP7g==", "incorrect_signature", "com.griffgriffgames.minesweeper")
      expectMsg(ValidationFailure)
    }
  }
}
