package io.parseq.examples.http4s

import java.util.concurrent.Executors

import org.http4s.client.dsl.io._
import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp, Resource}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{HttpRoutes, Request, Response, Uri}
import org.http4s.implicits._
import io.circe.generic.auto._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import cats.implicits._
import io.parseq.examples.http4s.ex05.{HashReq, HashResp}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._

object ex05_controller extends IOApp {

  val global: ExecutionContextExecutor = ExecutionContext
    .fromExecutor(Executors.newSingleThreadExecutor(new Thread(_, "worker-thread")))

  val client: Resource[IO, Client[IO]] = BlazeClientBuilder[IO](global)
    .withResponseHeaderTimeout(10.minutes)
    .resource

  val hashUri: Uri = Uri.uri("http://localhost:8181/hash")


  val computeService: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case req@POST -> Root / "compute" =>
      for {
        input <- req.as[HashReq]
        h1 <- hash(input.id, input.zeros, input.hex)
        h2 <- hash(input.id, input.zeros, h1.hex)
        h3 <- hash(input.id, input.zeros, h2.hex)
        resp <- Ok(List(h1, h2, h3))
      } yield resp
  }.orNotFound

  def hash(id: Long, zeros: Int, data: String): IO[HashResp] = client
    .use(_.expect[HashResp](POST(HashReq(id, zeros, data), hashUri)))
    .flatMap(r => IO {
      println(id + ":" + r + " " + Thread.currentThread())
      r
    })

  def run(args: List[String]): IO[ExitCode] = {
    val server = BlazeServerBuilder[IO]
      .withIdleTimeout(10.minutes)
      .bindHttp(8080, "localhost")
      .withConnectorPoolSize(10)
      .withHttpApp(computeService)
      .resource
      .use(_ => IO.never)

    server.as(ExitCode.Success)
  }
}
