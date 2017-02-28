package fr.ramiro.gocd.dns

import org.json4s.JsonAST.JValue
import org.json4s.{DefaultWriters, JValue, Writer}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

package object poller {
  case class GoField(
    displayName: String,
    displayOrder: Int,
    partOfIdentity: Boolean,
    required: Boolean
  ) extends annotation.StaticAnnotation

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

  import scala.reflect.runtime.universe._
  def listGoFields[T](implicit t: TypeTag[T]): List[(String, GoField)] = {
    def getGoFieldInstanceParams(goFieldAnnotation: Annotation) = {
      goFieldAnnotation.tree.children.tail.collect {
        case a if a.productElement(0).isInstanceOf[Constant] =>
          a.productElement(0).asInstanceOf[Constant].value
      }
    }

    def getGoFieldInstance(goFieldAnnotation: Annotation): GoField = {
      t.mirror
        .reflectClass(goFieldAnnotation.tree.tpe.typeSymbol.asClass)
        .reflectConstructor(goFieldAnnotation.tree.tpe.decl(termNames.CONSTRUCTOR).asMethod)(getGoFieldInstanceParams(goFieldAnnotation): _*)
        .asInstanceOf[GoField]
    }

    typeOf[T].members
      .collect { case s: TermSymbol => s }
      .filter(s => s.isVal || s.isVar)
      .flatMap {
        f =>
          f.annotations.find(_.tree.tpe =:= typeOf[GoField]).map {
            f.name.toString -> getGoFieldInstance(_)
          }
      }.toList
  }

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
