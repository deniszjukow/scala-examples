package io.parseq.examples.fs2

import cats.effect._
import cats.effect.ExitCase._
import cats.implicits._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object ex18_fiber extends IOApp {

  val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  override def run(args: List[String]): IO[ExitCode] = {
    val launchMissiles: IO[Unit] = IO {
      println("missiles launched!")
      // throw new Exception(s"boom! [${Thread.currentThread().getName}]")
    }

    val runToBunker: IO[Unit] = IO {
      println(s"run to bunker! [${Thread.currentThread().getName}]")
      // throw new Exception("something bad happened...")
    }

    def print(msg: String) = IO(println(s"$msg [${Thread.currentThread().getName}]"))

    def fail(error: Throwable) = IO.raiseError[Unit](error)

    val program = for {
      fiber <- launchMissiles.start(cs)
      _ <- runToBunker.handleErrorWith { error =>
        print("something went wrong") *> fiber.cancel *> fail(error)
      }
      aftermath <- fiber.join
    } yield aftermath

    program.guaranteeCase {
      case Completed => IO(println("completed"))
      case Canceled => IO(println("cancelled"))
      case Error(error) => IO(println("error: " + error))
    }.as(ExitCode.Success)
  }
}
