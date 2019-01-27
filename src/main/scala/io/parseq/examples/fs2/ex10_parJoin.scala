package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO, Timer}
import fs2._
import _root_.io.parseq.examples.fs2.ex08_concurrent._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ex10_parJoin {

  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val r: Stream[Pure, Stream[IO, Int]] = Stream.range(0, 10).map { id =>
      Stream.range(0, 10)
        .through(randomDelays(1.second))
        .through(log(('a' + id).toChar.toUpper.toString))
    }

    r.parJoin(3).compile.drain.unsafeRunSync()
  }
}
