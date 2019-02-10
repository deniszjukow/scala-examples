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
import io.parseq.examples.http4s.ex05_protocol.User


import cats.data.Kleisli
import cats.effect.{ExitCode, IOApp}
import cats.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.implicits._
import cats.effect.IO
import io.circe.generic.auto._
import io.parseq.examples.http4s.ex05_protocol.{Hello, User}

object ex05_circe_server extends IOApp {

  val helloWorldService: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case req@POST -> Root / "hello" => for {
      user <- req.as[User]
      resp <- Ok(Hello(user.name + "!"))
    } yield resp
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] = {
    val server = BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(helloWorldService)
      .resource
      .use(_ => IO.never)

    server.as(ExitCode.Success)
  }
}