package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO, Timer}
import fs2._
import fs2.concurrent.Queue

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._
import mpilquist06_concurrent._


object playground_queue {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val tm: Timer[IO] = IO.timer(ec)
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  case class Buff(q1: Queue[IO, Int], q2: Queue[IO, Either[Throwable, String]]) {
    def start: Stream[IO, Unit] = {
      val s1: Stream[IO, Unit] = Stream.range(0, 1000).through(q1.enqueue)
      val s2: Stream[IO, Unit] = q1.dequeue.evalMap(x => IO(s"[$x]=${1000 / (System.currentTimeMillis % 7)}").attempt).through(q2.enqueue)
      val s3: Stream[IO, Unit] = q2.dequeue.evalMap[IO, Unit](x => IO(println(x))).through(randomDelays(1.millis))
      Stream(s1, s2, s3).parJoin(10)
    }
  }

  def main(args: Array[String]): Unit = {
    val program = for {
      q1 <- Stream.eval(Queue.bounded[IO, Int](1))
      q2 <- Stream.eval(Queue.bounded[IO, Either[Throwable, String]](100))
      b = Buff(q1, q2)
      _ <- b.start
    } yield ()
    program.handleErrorWith(x => Stream.emit(x.getMessage)).compile.drain.unsafeRunSync()
  }
}
