package io.parseq.examples.fs2

import fs2._

object mpilquist04_chunks {
  def main(args: Array[String]): Unit = {
    // chunks
    val s0: Stream[Pure, Chunk[Int]] = Stream(1, 2, 3).chunks
    println(s0.toList)

    val s1: Stream[Pure, Any] = Stream(1, 2, 3) ++ Stream(4, 5, 6)
    println(s1.chunks.toList)

    val s2 = Stream(1, 2, 3).repeat.take(10).chunks.toList
    println(s2)
  }
}
