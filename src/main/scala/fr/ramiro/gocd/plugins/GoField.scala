package fr.ramiro.gocd.plugins

case class GoField(
  name: String,
  displayName: String,
  displayOrder: Int = 0,
  partOfIdentity: Boolean = true,
  required: Boolean = true
) extends annotation.StaticAnnotation

object GoField{
  import scala.reflect.runtime.universe._

  def listGoFields[T](implicit t: TypeTag[T], tt: TypeTag[GoField]): List[(String, GoField)] = {
    val cm = t.mirror
    val im = cm reflect (cm reflectModule (cm classSymbol cm.runtimeClass(tt.tpe)).companion.asModule).instance
    val const = cm.reflectClass(tt.tpe.typeSymbol.asClass).reflectConstructor(tt.tpe.decl(termNames.CONSTRUCTOR).asMethod)

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
}