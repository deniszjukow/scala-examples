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

case class Channel[F[_]](left: Queue[F, WebSocketFrame], right: Queue[F, WebSocketFrame]) {
  def flip: Channel[F] = Channel[F](right, left)
}

class ChatApp[F[_]](q1: Queue[F, WebSocketFrame], q2: Queue[F, WebSocketFrame])(implicit F: ConcurrentEffect[F], timer: Timer[F]) extends Http4sDsl[F] {

  var ch: Channel[F] = _

  val channel: F[Channel[F]] = F.defer[Channel[F]] {
    synchronized {
      if (ch == null) {
        for {
          x <- Queue.unbounded[F, WebSocketFrame]
          y <- Queue.unbounded[F, WebSocketFrame]
          channel = Channel(x, y)
        } yield F.pure(channel)
      } else {
        val c = ch
        ch = null
        F.pure(c.flip)
      }
    }
  }

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / name => for {
      ch <- channel
      ws <- WebSocketBuilder[F].build(ch.left.dequeue, ch.right.enqueue)
    } yield ws
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
