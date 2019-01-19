package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO}
import fs2.concurrent.SignallingRef
import mpilquist06_concurrent._

import scala.concurrent.ExecutionContext

object mpilquist08_signal_unsafe {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def absurd[A]: A = absurd

  def main(args: Array[String]): Unit = {

    val x: IO[SignallingRef[IO, Int]] = SignallingRef[IO, Int](1)

    val s: SignallingRef[IO, Int] = x.unsafeRunSync()

    s.discrete
      .through(log("discrete"))
      .compile.toList.unsafeToFuture()

    s.set(2).unsafeRunSync()

    s.modify[Int](s => (s + 1, s + 1)).unsafeRunSync()

    absurd[Nothing]
  }
}
