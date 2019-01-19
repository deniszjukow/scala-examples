package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO, Timer}
import fs2._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object mpilquist05_time {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val ticks: Stream[IO, Unit] = Stream
      .awakeEvery[IO](1.second)
      .evalMap(x => IO(println(x)))

    val sleep: Stream[IO, Boolean] = Stream.eval(IO.sleep(5.seconds).map(_ => true))
    ticks.interruptWhen(sleep).compile.drain.unsafeRunSync()
  }
}
