package io.parseq.examples.http4s

object ex05 {

  case class Hello(name: String)

  case class User(name: String)

  case class Input(zeros: Int, hex: String)

  case class HashReq(id: Long, zeros: Int, hex: String)

  case class HashResp(salt: String, hex: String)

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
  }
}
