package fr.ramiro.gocd

import java.text.SimpleDateFormat
import java.util.Date

import org.json4s.JsonAST.JString
import org.json4s.{ DefaultReaders, JObject, JValue, Reader, Writer }
import org.json4s.JsonDSL._

import scala.language.implicitConversions

package object plugins {
  case class ValidationError(key: String, message: String)

  object ValidationError {
    implicit object ValidateResponseElementWriter extends Writer[ValidationError] {
      override def write(obj: ValidationError): JValue = ("key" -> obj.key) ~ ("message" -> obj.message)
    }
  }

  case class StatusResponse(status: Boolean, messages: Seq[String])

  object StatusResponse {
    implicit object StatusResponseWriter extends Writer[StatusResponse] {
      override def write(obj: StatusResponse): JValue = ("status" -> (if (obj.status) "success" else "failure")) ~ ("messages" -> obj.messages)
    }
  }

  case class PackageRevision(
    revision: String,
    timestamp: Date,
    user: Option[String] = None,
    revisionComment: Option[String] = None,
    trackbackUrl: Option[String] = None,
    data: Map[String, String] = Map.empty
  )

  object PackageRevision {
    def dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    implicit def goDateWriter(obj: Date): JValue = JString(dateFormat.format(obj))

    implicit object PackageRevisionWriter extends Writer[PackageRevision] with Reader[PackageRevision] with DefaultReaders {
      override def write(obj: PackageRevision): JValue = {
        ("revision" -> obj.revision) ~
          ("timestamp" -> obj.timestamp) ~
          obj.user.fold(JObject()) {
            "user" -> _
          } ~ obj.revisionComment.fold(JObject()) {
            "revisionComment" -> _
          } ~ obj.trackbackUrl.fold(JObject()) {
            "trackbackUrl" -> _
          } ~ ("data" -> obj.data)
      }

      override def read(value: JValue): PackageRevision = PackageRevision(
        revision = (value \ "revision").as[String],
        timestamp = dateFormat.parse((value \ "timestamp").as[String]),
        user = (value \ "user").as[Option[String]],
        revisionComment = (value \ "revisionComment").as[Option[String]],
        trackbackUrl = (value \ "trackbackUrl").as[Option[String]],
        data = (value \ "trackbackUrl").as[Map[String, String]]
      )
    }
  }

  implicit object ConfigurationFieldWriter extends Writer[Seq[GoField]] {
    override def write(fields: Seq[GoField]): JValue = {
      fields.foldRight(JObject()) {
        (obj, a) =>
          a ~ {
            obj.name -> {
              ("display-name" -> obj.displayName) ~
                ("display-order" -> obj.displayOrder) ~
                ("required" -> obj.required) ~
                ("part-of-identity" -> obj.partOfIdentity) ~ {
                  if (obj.defaultValue.nonEmpty) {
                    "default-value" -> obj.defaultValue: JObject
                  } else {
                    JObject()
                  }
                }
            }
          }
      }
    }
  }
}
