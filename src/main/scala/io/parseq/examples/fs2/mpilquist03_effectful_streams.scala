package io.parseq.examples.fs2

import fs2._
import cats.effect.{ContextShift, IO, Resource}

object mpilquist03_effectful_streams {
  def main(args: Array[String]): Unit = {
    val io01: IO[Long] = IO {
      System.currentTimeMillis()
    }

    // eval
    println(Stream.eval(io01).compile.toList.unsafeRunSync())

    // repeatEval
    println(Stream.repeatEval(io01).take(10).compile.toList.unsafeRunSync())
  }
}
