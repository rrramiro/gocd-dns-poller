package fr.ramiro.gocd.plugins

import org.json4s.{ JObject, JValue, Writer }
import org.json4s.JsonDSL._
import scala.reflect.runtime.universe._

case class GoField(
  name: String,
  displayName: String,
  displayOrder: Int = 0,
  defaultValue: Option[String] = None,
  partOfIdentity: Boolean = true,
  required: Boolean = true
) extends annotation.StaticAnnotation

object GoField {

  def listGoFields[T](clazz: Class[T]): List[(String, GoField)] = {
    val cm = runtimeMirror(clazz.getClassLoader)
    val ttt = cm.classSymbol(clazz).toType
    val tf = cm.classSymbol(classOf[GoField]).toType
    val im = cm reflect (cm reflectModule (cm classSymbol cm.runtimeClass(tf)).companion.asModule).instance
    val const = cm.reflectClass(tf.typeSymbol.asClass).reflectConstructor(tf.decl(termNames.CONSTRUCTOR).asMethod)

    def getGoFieldInstance(goFieldAnnotation: Annotation) = {
      const {
        goFieldAnnotation.tree.children.tail.collect {
          case a if a.productElement(0).isInstanceOf[Constant] =>
            a.productElement(0).asInstanceOf[Constant].value
          case a =>
            val as = a.productElement(1).asInstanceOf[TermName]
            (im reflectMethod (im.symbol.typeSignature member as).asMethod)()
        }: _*
      }.asInstanceOf[GoField]
    }

    ttt
      .members
      .collect { case s: TermSymbol => s }
      .filter(s => s.isVal || s.isVar)
      .flatMap {
        f =>
          f.annotations.find(_.tree.tpe =:= typeOf[GoField]).map {
            f.name.decodedName.toString.trim -> getGoFieldInstance(_)
          }
      }.toList
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
                ("part-of-identity" -> obj.partOfIdentity) ~
                obj.defaultValue.fold(JObject()) {
                  "default-value" -> _
                }
            }
          }
      }
    }
  }
}