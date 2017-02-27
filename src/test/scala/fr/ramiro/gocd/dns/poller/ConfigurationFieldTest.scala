package fr.ramiro.gocd.dns.poller

import org.scalatest.FunSuite
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

class ConfigurationFieldTest extends FunSuite with GoPluginWritersAndReaders {
  test("To JSON"){
    assert(pretty(asJValue(Seq(GoPluginDnsPoller.DNS_SERVER))) ===
      """{
        |  "DNS_SERVER" : {
        |    "display-name" : "DNS Server",
        |    "display-order" : 0,
        |    "required" : true,
        |    "part-of-identity" : true
        |  }
        |}""".stripMargin)
  }
}
