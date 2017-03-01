package fr.ramiro.gocd.dns

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultWriters, JValue, Writer}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

package object poller {


  case class RepositoryConfigurationField(
    name: String,
    displayName: String,
    displayOrder: Int,
    partOfIdentity: Boolean = true,
    required: Boolean = true
  )

  case class PackageConfigurationField(
    name: String,
    displayName: String,
    displayOrder: Int,
    defaultValue: String,
    partOfIdentity: Boolean = true,
    required: Boolean = true
  )

  case class ValidationError(key: String, message: String)

  case class StatusResponse(status: Boolean, messages: Seq[String])



  trait GoPluginWritersAndReaders extends DefaultWriters with DefaultReaders {

    implicit object StatusResponseWriter extends Writer[StatusResponse] {
      override def write(obj: StatusResponse): JValue = ("status" -> (if (obj.status) "success" else "failure")) ~ ("messages" -> obj.messages)
    }

    implicit object ValidateResponseElementWriter extends Writer[ValidationError] {
      override def write(obj: ValidationError): JValue = ("key" -> obj.key) ~ ("message" -> obj.message)
    }

    implicit object RepositoryConfigurationFieldWriter extends Writer[Seq[RepositoryConfigurationField]] {
      override def write(fields: Seq[RepositoryConfigurationField]): JValue = {
        fields.foldRight(JObject()) {
          (obj, a) =>
            a ~ {
              obj.name -> {
                ("display-name" -> obj.displayName) ~
                  ("display-order" -> obj.displayOrder) ~
                  ("required" -> obj.required) ~
                  ("part-of-identity" -> obj.partOfIdentity)
              }
            }
        }
      }
    }

    implicit object PackageConfigurationFieldWriter extends Writer[Seq[PackageConfigurationField]] {
      override def write(fields: Seq[PackageConfigurationField]): JValue = {
        fields.foldRight(JObject()) {
          (obj, a) =>
            a ~ {
              obj.name -> {
                ("display-name" -> obj.displayName) ~
                  ("display-order" -> obj.displayOrder) ~
                  ("default-value" -> obj.defaultValue) ~
                  ("required" -> obj.required) ~
                  ("part-of-identity" -> obj.partOfIdentity)
              }
            }
        }
      }
    }
  }
}
