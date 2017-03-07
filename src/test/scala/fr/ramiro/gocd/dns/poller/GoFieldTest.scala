package fr.ramiro.gocd.dns.poller

import fr.ramiro.gocd.plugins.{ PackageRevision, StatusResponse }
import org.json4s.jackson.JsonMethods.{ asJValue, pretty }
import org.scalatest.FunSuite

class GoFieldTest extends FunSuite {

  test("To JSON") {
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
