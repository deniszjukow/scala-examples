package io.parseq.examples.http4s

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}
import io.circe.generic.auto._
import io.circe.syntax._
import io.parseq.examples.http4s.ex05.HashReq
import org.http4s.Uri
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.POST

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random

object ex06_fire_and_forget extends IOApp {

  def log(s: String): Unit = println(s"$s ${Thread.currentThread()}")

  val address: Uri = Uri.uri("http://localhost:8080/compute")
  val str: String = "0123456789"

  def sendRequest(streamId: String, id: Int): IO[Unit] = {
    val complexity = Random.nextInt(5)
    val req = POST(HashReq(id, complexity, str).asJson, address)
    val resource = BlazeClientBuilder[IO](global)
      .withRequestTimeout(10.minutes)
      .withResponseHeaderTimeout(10.minutes)
      .resource

    resource.use(o => o.expect[String](req))
      .flatMap(r => IO(println(s"$streamId:$id: ${Thread.currentThread().getName} ${System.currentTimeMillis()}: result: $r")))
  }

  override def run(args: List[String]): IO[ExitCode] = {

    def ints(streamId: String): Stream[IO, Unit] = Stream.unfold[Pure, Int, Int](1)(int => Option(int, int + 1))
      .map(int => (streamId, int))
      .evalMap { case (s, x) => sendRequest(s, x) }

    Stream("a", "b", "c", "d", "e", "f").map(x => ints(x))
      .parJoin(5)
      .compile.drain.map(_ => ExitCode.Success)
  }
}
