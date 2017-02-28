package fr.ramiro.gocd.dns.poller

import org.scalatest.FunSuite
import scala.annotation.meta.field

class GoFieldTest extends FunSuite {
  case class ConfigurationObject(
    @(GoField @field)(name = "FIELD_1", displayName = "Field 1", displayOrder = 0) field1: String = "test",
    @(GoField @field)(name = "FIELD_2", displayName = "Field 2", displayOrder = 1) field2: Int = 1
  )

  test("GoField") {
    val annotations = listGoFields[ConfigurationObject]
    println("##########################")
    println(annotations)
    println("##########################")
    assert(annotations.size === 2)
  }
}
