package io.parseq.examples.fs2

import cats.effect.{ContextShift, IO}

import scala.concurrent.ExecutionContext

object mpilquist02_io {
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

    // async
    val io04: IO[Long] = IO.async[Long]{ e =>
      println(Thread.currentThread())
      e.apply(Right(System.currentTimeMillis()))
    }
    println("io04: " + io04.unsafeRunSync())
  }
}
