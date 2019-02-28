package com.gu.microserviceWorkshop

import io.circe.syntax._
import java.io._
import java.nio.charset.StandardCharsets.UTF_8

object Lambda {

  def handler(in: InputStream, out: OutputStream): Unit = {
    val response = APIResponse(200,  Map("Content-Type" -> "application/json"), "hello world again")
    //no spaces converts json to a string
    out.write(response.asJson.noSpaces.getBytes(UTF_8))
  }

}