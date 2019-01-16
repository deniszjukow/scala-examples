package io.parseq.fs2

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Resource}
import fs2.{Stream, io, text}

import scala.concurrent.ExecutionContext

object ex01_read_from_file {

  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val ec = Resource.make(IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))))(ec => IO(ec.shutdown()))

  val converter: Stream[IO, String] = Stream.resource(ec).flatMap { blocking =>
    io.file.readAll[IO](Paths.get("C:/Users/denis/dev/data/shakespeare.txt"), blocking, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
  }

  def main(args: Array[String]): Unit = {
    println(converter.take(5).compile.toList.unsafeRunSync())
  }
}