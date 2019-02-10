package io.parseq.examples.http4s

import java.security.MessageDigest

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration._
import io.circe.generic.auto._
import io.parseq.examples.http4s.ex05._
import io.parseq.examples.http4s.ex05_protocol.{HashReq, HashResp}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Request, Response}

import scala.annotation.tailrec

object ex05_worker extends IOApp {


  def hash(zeros: Int, data: String): (String, BigInt) = {
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    @tailrec
    def go(salt: BigInt): (String, BigInt) = {
      val salted = data + bytes2hex(salt.toByteArray)
      val hash = try {
        val b = hex2bytes(salted)
        bytes2hex(digest.digest(b))
      } catch {
        case e: ArrayIndexOutOfBoundsException =>
          println("ERROR: " + salted)
          throw e
      }
      if (hash.startsWith("0" * zeros)) (hash, salt)
      else go(salt + BigInt(1))
    }

    go(BigInt(0))
  }

  val hashService: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case req@POST -> Root / "hash" => for {
      data <- req.as[HashReq]
      hs <- IO(hash(data.zeros, data.hex))
      _ <- IO(println(hs._1))
      resp <- Ok(HashResp(hs._2.toString, hs._1))
    } yield resp
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    val server = BlazeServerBuilder[IO]
      .withIdleTimeout(10.minutes)
      .bindHttp(8181, "localhost")
      .withHttpApp(hashService)
      .resource
      .use(_ => IO.never)

    server.as(ExitCode.Success)
  }
}