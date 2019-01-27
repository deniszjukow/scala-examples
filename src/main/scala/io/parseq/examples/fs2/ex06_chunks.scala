package io.parseq.examples.fs2

import fs2._

object ex06_chunks {
  def main(args: Array[String]): Unit = {
    // chunks
    val s01: Stream[Pure, Chunk[Int]] = Stream(1, 2, 3).chunks
    println(s01.toList)

    val s02: Stream[Pure, Any] = Stream(1, 2, 3) ++ Stream(4, 5, 6)
    println(s02.chunks.toList)

    val s03 = Stream(1, 2, 3).repeat.take(10).chunks.toList
    println(s03)
  }
}
