package io.parseq.examples.http4s

import io.circe.syntax._
import org.http4s.dsl.io._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._
import cats.effect.{ExitCode, IO, IOApp}
import io.circe.generic.auto._
import org.http4s.circe._
import cats.implicits._
import io.parseq.examples.http4s.ex05_protocol.HashReq

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object ex05_circe_client1 extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val address = Uri.uri("http://localhost:8080/compute")
    val str = "0123456789abcdef"

    fs2.Stream.repeatEval[IO, Unit] {
      val req = POST(HashReq(100 + Random.nextInt(100), 3, str).asJson, address)
      val resource = BlazeClientBuilder[IO](global)
        .withRequestTimeout(10.minutes)
        .withResponseHeaderTimeout(10.minutes)
        .resource

      resource.use(_.expect[String](req))
        .flatMap(r => IO(println(r)))
    }.compile.drain.as(ExitCode.Success)
  }
}
