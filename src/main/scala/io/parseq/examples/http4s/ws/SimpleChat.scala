package io.parseq.examples.http4s.ws

import cats.effect._
import cats.implicits._
import fs2._
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame

object Chat extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      q1 <- Queue.unbounded[IO, WebSocketFrame]
      q2 <- Queue.unbounded[IO, WebSocketFrame]
    } yield ChatApp[IO](q1, q2)
  }.flatMap(y => y.stream.compile.drain.as(ExitCode.Success))
}

class ChatApp[F[_]](q1: Queue[F, WebSocketFrame], q2: Queue[F, WebSocketFrame])(implicit F: ConcurrentEffect[F], timer: Timer[F]) extends Http4sDsl[F] {

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "u1" => WebSocketBuilder[F].build(q1.dequeue, q2.enqueue)
    case GET -> Root / "u2" => WebSocketBuilder[F].build(q2.dequeue, q1.enqueue)
  }

  def stream: Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(8080)
      .withWebSockets(true)
      .withHttpApp(routes.orNotFound)
      .serve
}

object ChatApp {
  def apply[F[_] : ConcurrentEffect : Timer](u1: Queue[F, WebSocketFrame], u2: Queue[F, WebSocketFrame]): ChatApp[F] =
    new ChatApp[F](u1, u2)
}
