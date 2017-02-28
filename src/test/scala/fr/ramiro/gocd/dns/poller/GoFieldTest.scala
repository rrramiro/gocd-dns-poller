package fr.ramiro.gocd.dns.poller

import org.scalatest.FunSuite

import scala.annotation.meta.field


case class Person(name: String, age: Int = 18) {
  require(age >= 18)
}

class GoFieldTest extends FunSuite {
  case class ConfigurationObject(
    @(GoField @field)(displayName = "Field 1", displayOrder = 0, partOfIdentity = true, required = true) field1: String = "test",
    @(GoField @field)(displayName = "Field 2", displayOrder = 1, partOfIdentity = true, required = true) field2: Int = 1
  )

  test("GoField") {
    val expected = Set(
      GoField(displayName = "Field 1", displayOrder = 0, partOfIdentity = true, required = true),
      GoField(displayName = "Field 2", displayOrder = 1, partOfIdentity = true, required = true)
    )
    val annotations = listGoFields[ConfigurationObject].toMap
    assert(annotations.values.toSet === expected)
  }

  test("create"){
    assert(Person(name = null) === newCase[Person]())
  }

  import scala.reflect.runtime.universe._

  def newCase[A]()(implicit t: TypeTag[A]): A = {
    val cm = t.mirror
    val im = cm reflect (cm reflectModule (cm classSymbol cm.runtimeClass(t.tpe)).companion.asModule).instance
    defaut[A](im, "apply")
  }

  def defaut[A](im: InstanceMirror, name: String): A = {
    val at = TermName(name)
    val ts = im.symbol.typeSignature
    val method = (ts member at).asMethod

    // either defarg or default val for type of p

    val args = (for (ps <- method.paramLists; p <- ps) yield p).zipWithIndex map {
      case (p, i) =>
        val defarg = ts member TermName(s"$name$$default$$${i+1}")
        if (defarg != NoSymbol) {
          println(s"default $defarg")
          (im reflectMethod defarg.asMethod)()
        } else {
          println(s"def val for $p")
          p.typeSignature match {
            case t if t =:= typeOf[String] => null
            case t if t =:= typeOf[Int]    => 0
            case x                        => throw new IllegalArgumentException(x.toString)
          }
        }
    }
    (im reflectMethod method)(args: _*).asInstanceOf[A]
  }
}
