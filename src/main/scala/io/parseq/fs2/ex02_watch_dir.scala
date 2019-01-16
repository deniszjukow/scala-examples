package io.parseq.fs2

import java.nio.file.Paths
import java.util.concurrent.Executors
import cats.effect.{ContextShift, IO, Resource}
import cats.implicits._
import fs2.Sink
import fs2.io.Watcher
import scala.concurrent.ExecutionContext
import Watcher.EventType._

object ex02_watch_dir {

  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def main(args: Array[String]): Unit = {
    fs2.io.file.watch[IO](
      path = Paths.get("C:\\Users\\denis\\Temp"),
      types = Seq(Created, Deleted, Modified), modifiers=Seq())
      .map(_.toString)
      .to[IO](Sink.showLinesStdOut)
      .compile.toList.unsafeRunSync()
  }
}
