package io.parseq.examples.fs2

import cats.effect.{Concurrent, IO, Timer}
import fs2.Pipe

import scala.concurrent.duration.FiniteDuration
import scala.util.Random
import scala.concurrent.duration._
import scala.language.higherKinds

object ex08_concurrent {

  def log[F[_], A](prefix: String)(implicit F: Concurrent[F]): Pipe[F, A, A] = _.evalMap { a =>
    F.delay { println(s"$prefix> $a"); a }
  }

  def randomDelays[A](maxDelay: FiniteDuration)(implicit timer: Timer[IO]): Pipe[IO, A, A] = _.evalMap { a =>
    IO.sleep(Random.nextInt(maxDelay.toMillis.toInt).toLong.millis).map(_ => a)
  }
}
