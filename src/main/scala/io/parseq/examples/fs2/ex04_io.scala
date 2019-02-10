package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO}
import cats.implicits._

import scala.concurrent.ExecutionContext

object ex04_io {
  def main(args: Array[String]): Unit = {

    // pure
    val io01 = IO.pure(42)

    // delay (same thread)
    val io02 = IO.delay {
      println("hello")
      System.currentTimeMillis
    }
    println(io02.unsafeRunSync())

    // apply (same as delay)
    val io03 = IO {
      println("good bye")
      System.currentTimeMillis
    }
    println(io03.unsafeRunSync())

    // async ("surprisingly", still the same thread!!!)
    val io04: IO[Long] = IO.async[Long]{ cb =>
      println(Thread.currentThread())
      cb(Right(System.currentTimeMillis()))
    }
    println("io04: " + io04.unsafeRunSync())

    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    // shift (separate thread)
    val io05: IO[Long] = IO.shift *> IO {
      println(Thread.currentThread())
      System.currentTimeMillis()
    }
    println("io05: " + io05.unsafeRunSync())
  }
}
