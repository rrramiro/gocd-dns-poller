package fr.ramiro.gocd.dns.poller

import fr.ramiro.gocd.plugins.GoField
import org.scalatest.FunSuite

import scala.annotation.meta.field

class GoFieldTest extends FunSuite {
  case class ConfigurationObject(
    @(GoField @field)(name = "FIELD_1", displayName = "Field 1") field1: String = "test",
    @(GoField @field)(name = "FIELD_2", displayName = "Field 2", displayOrder = 1) field2: Int = 1
  )

  test("GoField") {
    val expected = Set(
      GoField(name = "FIELD_1", displayName = "Field 1"),
      GoField(name = "FIELD_2", displayName = "Field 2", displayOrder = 1)
    )
    val annotations = GoField.listGoFields[ConfigurationObject].toMap
    assert(annotations.values.toSet === expected)
  }
}
