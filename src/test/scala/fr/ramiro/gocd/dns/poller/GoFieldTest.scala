package fr.ramiro.gocd.dns.poller

import fr.ramiro.gocd.plugins.{GoField, StatusResponse}
import org.json4s.jackson.JsonMethods.{asJValue, pretty}
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

  test("To JSON"){
    assert(pretty(asJValue(Seq(GoField("DNS_SERVER", "DNS Server", 0)))) ===
    """{
      |  "DNS_SERVER" : {
      |    "display-name" : "DNS Server",
      |    "display-order" : 0,
      |    "required" : true,
      |    "part-of-identity" : true
      |  }
      |}""".stripMargin)


    assert(pretty(asJValue(Seq(GoField("DNS_SERVER", "DNS Server", 0, Some("default"), true, true)))) ===
    """{
      |  "DNS_SERVER" : {
      |    "display-name" : "DNS Server",
      |    "display-order" : 0,
      |    "required" : true,
      |    "part-of-identity" : true,
      |    "default-value" : "default"
      |  }
      |}""".stripMargin)


    assert(pretty(asJValue(StatusResponse(status = true, Seq("message")))) ===
    """{
      |  "status" : "success",
      |  "messages" : [ "message" ]
      |}""".stripMargin)

  }
}
