package fr.ramiro.gocd

import org.json4s.{JValue, Writer}
import org.json4s.JsonDSL._

package object plugins {
  case class ValidationError(key: String, message: String)

  object ValidationError {
    implicit object ValidateResponseElementWriter extends Writer[ValidationError] {
      override def write(obj: ValidationError): JValue = ("key" -> obj.key) ~ ("message" -> obj.message)
    }
  }

  case class StatusResponse(status: Boolean, messages: Seq[String])

  object StatusResponse{
    implicit object StatusResponseWriter extends Writer[StatusResponse] {
      override def write(obj: StatusResponse): JValue = ("status" -> (if (obj.status) "success" else "failure")) ~ ("messages" -> obj.messages)
    }
  }

}
