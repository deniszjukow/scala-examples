package io.parseq.examples.http4s

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{Pure, Stream}
import org.http4s.Uri

import scala.concurrent.duration._
import scala.util.Random

object ex06_fire_and_forget_with_sleep extends IOApp {

  def log(s: String): Unit = println(s"$s ${Thread.currentThread()}")

  def io(x: String): IO[Unit] = for {
    r <- IO {
      val time = Random.nextInt(1000).toLong.millis
      log(s"value: $x, $time")
      time
    }
    _ <- IO.sleep(r)
  } yield ()

  val address: Uri = Uri.uri("http://localhost:8080/compute")
  val str: String = "0123456789"

  override def run(args: List[String]): IO[ExitCode] = {

    def ints(streamId: String): Stream[IO, Unit] = Stream.unfold[Pure, Int, Int](1)(int => Option(int, int + 1))
      .map(int => (streamId, int))
      .evalMap { case (_, x) => io(x.toString) }

    Stream("a", "b", "c", "d", "e", "f").map(x => ints(x))
      .parJoin(5)
      .compile.drain.map(_ => ExitCode.Success)
  }
}
