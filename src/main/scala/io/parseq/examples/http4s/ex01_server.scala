package io.parseq.examples.http4s

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._

object ex01_server extends IOApp {

  val helloWorldService: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(helloWorldService)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}