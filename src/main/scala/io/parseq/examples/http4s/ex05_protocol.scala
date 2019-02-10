package io.parseq.examples.http4s

object ex05_protocol {

  case class Hello(name: String)

  case class User(name: String)

  case class Input(zeros: Int, hex: String)

  case class HashReq(id: Long, zeros: Int, hex: String)

  case class HashResp(salt: String, hex: String)

}
