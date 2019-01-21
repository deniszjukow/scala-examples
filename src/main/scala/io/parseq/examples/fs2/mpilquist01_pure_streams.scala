package io.parseq.examples.fs2

import fs2._

object mpilquist01_pure_streams {
  def main(args: Array[String]): Unit = {

    // create
    val s01: Stream[Pure, Int] = Stream(1, 2, 3)
    // same as: val s01:Stream[Pure, Int] = Stream.emits(List(1, 2, 3))
    println(s01.toList)

    // map
    val s02 = Stream(1, 2, 3).map(x => (x + 1) + "!")
    println(s02.toList)

    // flatMap
    val s03 = Stream(1, 2, 3).flatMap(x => Stream.emits(List.fill(x)(x)))
    println(s03.toList)

    // interleave
    val s04 = Stream(1, 2, 3) interleave Stream(4, 5, 6, 0, 0)
    println(s04.toList)
    // List(1, 4, 2, 5, 3, 6)

    // intersperse
    val s05 = Stream(1, 2, 3).intersperse(42)
    println(s05.toList)

    // zip and repeat
    val s06 = Stream(1, 2, 3, 4, 5).zip(Stream(0, 1).repeat)
    println(s06.toList)
  }
}
