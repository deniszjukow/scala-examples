package io.parseq.examples.fs2

import cats.effect.{Concurrent, ContextShift, IO, Timer}
import fs2.concurrent.{SignallingRef, Topic}
import fs2.{Pipe, Stream}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.language.higherKinds


object playground_topic {

  sealed trait Event

  case class Text(text: String) extends Event

  case object Quit extends Event

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val tm: Timer[IO] = IO.timer(ec)

  case class EventService[F[_]](
    eventTopic: Topic[F, Event],
    interrupter: SignallingRef[F, Boolean])(
    implicit F: Concurrent[F], timer: Timer[F]
  ) {
    def startPublisher: Stream[F, Unit] = {
      val textEvents: Stream[F, Unit] = eventTopic.publish(Stream
        .awakeEvery[F](1.second)
        .evalMap(t => F.delay(Text(t.toString + Thread.currentThread().getName)))
      )
      val quitEvent = Stream.eval(eventTopic.publish1(Quit))
      (textEvents.take(15) ++ quitEvent ++ textEvents).interruptWhen(interrupter)
    }

    def startSubscribers: Stream[F, Unit] = {
      val s1 = Stream.sleep_[F](0.seconds) ++ eventTopic.subscribe(5)
      val s2 = Stream.sleep_[F](5.seconds) ++ eventTopic.subscribe(5)
      val s3 = Stream.sleep_[F](10.seconds) ++ eventTopic.subscribe(5)

      def sink(n: Int): Pipe[F, Event, Unit] = _.flatMap {
        case Text(text) => Stream.eval(F.delay(println(s"$n : $text")))
        case Quit => Stream.eval(interrupter.set(true))
        case x => Stream.eval(F.delay(println(s"$n : $x")))
      }

      Stream(
        s1.through(sink(1)),
        s2.through(sink(2)),
        s3.through(sink(3))
      ).parJoin(3)
    }
  }

  def main(args: Array[String]): Unit = {
    val x = for {
      topic <- Stream.eval(Topic[IO, Event](Text("initial message")))
      signal <- Stream.eval(SignallingRef[IO, Boolean](false))
      service = EventService[IO](topic, signal)
      _ <- Stream(
        service.startPublisher concurrently service.startSubscribers
      ).parJoin(3)
    } yield ()
    x.compile.drain.unsafeRunSync()
  }
}
