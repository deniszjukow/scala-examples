package io.parseq.examples.fs2

import _root_.io.parseq.examples.fs2.mpilquist06_concurrent._
import cats.effect.{ContextShift, IO, Timer}
import fs2._
import fs2.concurrent.Queue

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object mpilquist10_queue {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  def main(args: Array[String]): Unit = {
    val program = Stream.eval(Queue.bounded[IO, Int](10)).flatMap { q =>
      val monitor = q.dequeue.through(log("dequeue"))
      val writer = Stream.range(0, 10)
        .through(randomDelays(1.second))
        .evalMap(v => q.enqueue1(v))
      monitor mergeHaltBoth writer
    }

    program.compile.drain.unsafeRunSync()
  }
}
