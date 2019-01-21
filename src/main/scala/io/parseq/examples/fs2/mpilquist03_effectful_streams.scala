package io.parseq.examples.fs2

import fs2._
import cats.effect.IO

object mpilquist03_effectful_streams {
  def main(args: Array[String]): Unit = {
    val io01: IO[Long] = IO {
      System.currentTimeMillis()
    }

    // eval
    val s01 = Stream.eval(io01)
    println(s01.compile.toList.unsafeRunSync())

    // repeatEval
    val s02 = Stream.repeatEval(io01).take(10)
    println(s02.compile.toList.unsafeRunSync())
  }
}
