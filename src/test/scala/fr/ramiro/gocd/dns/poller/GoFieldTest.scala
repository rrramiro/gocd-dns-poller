package fr.ramiro.gocd.dns.poller

import fr.ramiro.gocd.plugins.{ GoField, PackageRevision, StatusResponse }
import org.json4s.jackson.JsonMethods.{ asJValue, pretty }
import org.scalatest.FunSuite
import scala.reflect.runtime.universe._
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
    val annotations = GoField.listGoFields[ConfigurationObject](classOf[ConfigurationObject]).toMap
    assert(annotations.values.toSet === expected)
  }

  test("To JSON") {
    assert(pretty(asJValue(Seq(GoField("DNS_SERVER", "DNS Server")))) ===
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

    assert(pretty(asJValue(PackageRevision(
      revision = "abc-10.2.1.rpm",
      timestamp = PackageRevision.dateFormat.parse("2011-07-14T19:43:37.100Z"),
      user = Some("some-user"),
      revisionComment = Some("comment"),
      trackbackUrl = Some("http://localhost:9999"),
      data = Map(
        "VERSION" -> "5.3.0",
        "LOCATION" -> "http://www.sample.org/location/of/package",
        "DATA-THREE" -> "data-three-value"
      )
    ))) ===
      """{
        |  "revision" : "abc-10.2.1.rpm",
        |  "timestamp" : "2011-07-14T19:43:37.100Z",
        |  "user" : "some-user",
        |  "revisionComment" : "comment",
        |  "trackbackUrl" : "http://localhost:9999",
        |  "data" : {
        |    "VERSION" : "5.3.0",
        |    "LOCATION" : "http://www.sample.org/location/of/package",
        |    "DATA-THREE" : "data-three-value"
        |  }
        |}""".stripMargin)

  }
}
