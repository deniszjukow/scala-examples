package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO, Timer}
import fs2._
import _root_.io.parseq.examples.fs2.mpilquist06_concurrent._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object mpilquist07_parJoin {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val a = Stream.range(1, 10)
      .through(mpilquist06_concurrent.randomDelays(1.second))
      .through(log("A"))

    val b = Stream.range(1, 10)
      .through(randomDelays(1.second))
      .through(log("B"))

    val c = Stream.range(1, 10)
      .through(randomDelays(1.second))
      .through(log("C"))

    // val r: Stream[Pure, Stream[IO, Int]] = Stream(a, b, c)
    val r: Stream[Pure, Stream[IO, Int]] = Stream.range(0, 10).map { id =>
      Stream.range(0, 10)
        .through(randomDelays(1.second))
        .through(log(('a' + id).toChar.toUpper.toString))
    }

    r.parJoin(3).compile.toList.unsafeRunSync()
  }
}
