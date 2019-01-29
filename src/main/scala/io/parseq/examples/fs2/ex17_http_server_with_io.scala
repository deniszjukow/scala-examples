package io.parseq.examples.fs2

import java.io._
import java.net.{ServerSocket, Socket}

import cats.effect.ExitCase.{Canceled, Completed, Error}
import cats.effect._
import cats.effect.concurrent.MVar
import cats.effect.syntax.all._
import cats.implicits._

object ex17_http_server_with_io extends IOApp {

  def echoProtocol[F[_] : Sync](clientSocket: Socket, stopFlag: MVar[F, Unit]): F[Unit] = {

    def loop(reader: BufferedReader, writer: BufferedWriter): F[Unit] = for {
      line <- Sync[F].delay(reader.readLine())
      _ <- line match {
        case "STOP" => stopFlag.put(()) // Stop the server fiber
        case "" => Sync[F].unit // Empty line, we are done
        case _ => Sync[F].delay {
          writer.write(line + " : " + Thread.currentThread().getName)
          writer.newLine()
          writer.flush()
        } >> loop(reader, writer)
      }
    } yield ()

    def reader(clientSocket: Socket): Resource[F, BufferedReader] =
      Resource.make {
        Sync[F].delay(new BufferedReader(new InputStreamReader(clientSocket.getInputStream)))
      } { reader =>
        Sync[F].delay(reader.close()).handleErrorWith(_ => Sync[F].unit)
      }

    def writer(clientSocket: Socket): Resource[F, BufferedWriter] =
      Resource.make {
        Sync[F].delay(new BufferedWriter(new PrintWriter(clientSocket.getOutputStream)))
      } { writer =>
        Sync[F].delay(writer.close()).handleErrorWith(_ => Sync[F].unit)
      }

    def readerWriter(clientSocket: Socket): Resource[F, (BufferedReader, BufferedWriter)] =
      for {
        reader <- reader(clientSocket)
        writer <- writer(clientSocket)
      } yield (reader, writer)

    readerWriter(clientSocket).use { case (reader, writer) =>
      loop(reader, writer) // Let's get to work
    }
  }

  def serve[F[_] : Concurrent](serverSocket: ServerSocket, stopFlag: MVar[F, Unit]): F[Unit] = {

    def close(socket: Socket): F[Unit] = {
      Sync[F].delay(socket.close()).handleErrorWith(error => Sync[F].delay(println(error)))
    }

    for {
      socket <- Sync[F].delay(serverSocket.accept())
        .bracketCase { socket =>
          echoProtocol(socket, stopFlag)
            .guarantee(close(socket))
            .start >> Sync[F].pure(socket)
        } { (socket, exit) =>
          exit match {
            case Completed => Sync[F].unit
            case Error(_) | Canceled => close(socket)
          }
        }
      _ <- (stopFlag.read >> close(socket)).start
      _ <- serve(serverSocket, stopFlag)
    } yield ()
  }

  def server[F[_] : Concurrent](serverSocket: ServerSocket): F[ExitCode] = {
    for {
      stopFlag <- MVar[F].empty[Unit]
      serverFiber <- serve(serverSocket, stopFlag).start
      _ <- stopFlag.read
      _ <- serverFiber.cancel.start
    } yield ExitCode.Success
  }

  override def run(args: List[String]): IO[ExitCode] = {
    def close[F[_] : Sync](socket: ServerSocket): F[Unit] =
      Sync[F].delay(socket.close()).handleErrorWith(error => Sync[F].delay(println(error)))

    IO(new ServerSocket(5432)).bracket {
      serverSocket => server[IO](serverSocket) >> IO.pure(ExitCode.Success)
    } {
      serverSocket => close[IO](serverSocket) >> IO(println("Server finished"))
    }
  }
}
