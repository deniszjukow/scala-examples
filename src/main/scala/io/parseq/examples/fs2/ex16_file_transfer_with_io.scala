package io.parseq.examples.fs2

import java.io.{File, FileInputStream, FileOutputStream}

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.implicits._

object ex16_file_transfer_with_io extends IOApp {

  def copy[F[_]: Concurrent](origin: File, destination: File): F[Long] =
    inputOutputStream(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }

  def transmit[F[_]](in: FileInputStream, out: FileOutputStream, buff: Array[Byte], acc: Long)(implicit F: Sync[F]): F[Long] = {
    for {
      amount <- F.delay(in.read(buff, 0, buff.length))
      count <-
        if (amount > -1)
          F.delay(out.write(buff, 0, amount)) >> transmit(in, out, buff, acc + amount)
        else
          F.pure(acc)
    } yield count
  }

  def transfer[F[_]](in: FileInputStream, out: FileOutputStream)(implicit F: Concurrent[F]): F[Long] = {
    for {
      guard <- Semaphore[F](1)
      buff <- F.delay(new Array[Byte](1024 * 10))
      count <- guard.withPermit(transmit(in, out, buff, 0))
    } yield count
  }

  def inputStream[F[_]](file: File)(implicit F: Sync[F]): Resource[F, FileInputStream] =
    Resource.make {
      F.delay(new FileInputStream(file))
    } { stream =>
      F.delay(stream.close()).handleErrorWith(_ => F.unit)
    }

  def outputStream[F[_]](file: File)(implicit F: Sync[F]): Resource[F, FileOutputStream] =
    Resource.make {
      F.delay(new FileOutputStream(file))
    } { stream =>
      F.delay(stream.close()).handleErrorWith(_ => F.unit)
    }

  def inputOutputStream[F[_]: Sync](in: File, out: File): Resource[F, (FileInputStream, FileOutputStream)] =
    for {
      inputStream <- inputStream(in)
      outputStream <- outputStream(out)
    } yield (inputStream, outputStream)

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <-
        if (args.length < 2) IO.raiseError(new IllegalArgumentException("Need input and output arguments"))
        else IO.unit
      in = new File(args(n = 0))
      out = new File(args(n = 1))
      count <- copy[IO](in, out)
      _ <- IO(s"Copies $count bytes")
    } yield ExitCode.Success
  }
}
