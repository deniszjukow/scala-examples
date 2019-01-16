package io.parseq.http4s

import java.util.concurrent.Executors

import cats.effect._
import org.http4s.client._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.concurrent.ExecutionContext.global

object ex02_client {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  val blockingEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  val httpClient: Client[IO] = JavaNetClientBuilder[IO](blockingEC).create

  def main(args: Array[String]): Unit = {
    val hello: IO[String] = httpClient.expect[String]("http://localhost:8080/hello/Denis")
    println(hello.unsafeRunSync())
  }
}