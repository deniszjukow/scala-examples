package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO, Timer}
import fs2._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import mpilquist06_concurrent._

object mpilquist06_interleave_merge_either {

  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    val a = Stream.range(1, 10)
      .through(mpilquist06_concurrent.randomDelays(1.second))
      .through(log("A"))

    val b = Stream.range(1, 10)
      .through(randomDelays(1.second))
      .through(log("B"))

    //    val r = (a interleave b).through(log("interleaved"))
    //    val r = (a merge b).through(log("merged"))
    val r = (a either b).through(log("either"))

    r.compile.toList.unsafeRunSync()
  }
}
