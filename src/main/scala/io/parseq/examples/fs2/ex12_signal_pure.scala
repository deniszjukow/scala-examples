package io.parseq.examples.fs2

import _root_.io.parseq.examples.fs2.ex08_concurrent._
import cats.effect.{ContextShift, IO, Timer}
import fs2._
import fs2.concurrent.SignallingRef

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ex12_signal_pure {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val program: Stream[IO, Unit] = Stream.eval(SignallingRef[IO, Int](0)).flatMap { s =>
      val monitor: Stream[IO, INothing] = s.discrete.through(log("monitor")).drain
      val writer = Stream.range(0, 10)
        .through(randomDelays(1.second))
        .evalMap(v => s.set(v))
      monitor mergeHaltBoth writer
    }

    program.compile.drain.unsafeRunSync()
  }
}
