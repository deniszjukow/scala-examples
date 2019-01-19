package io.parseq.examples.fs2

import cats.effect.{IO, Timer}
import fs2.Pipe

import scala.concurrent.duration.FiniteDuration
import scala.util.Random
import scala.concurrent.duration._

object mpilquist06_concurrent {

  def log[A](prefix: String): Pipe[IO, A, A] = _.evalMap { a =>
    IO {
      println(s"$prefix> $a")
      a
    }
  }

  def randomDelays[A](maxDelay: FiniteDuration)(implicit timer: Timer[IO]): Pipe[IO, A, A] = _.evalMap { a =>
    IO.sleep(Random.nextInt(maxDelay.toMillis.toInt).toLong.millis).map(_ => a)
  }
}
