package fr.ramiro.gocd.dns.poller

import org.scalatest.FunSuite

class GoPluginDnsPollerTest extends FunSuite {
  test("validateRepositoryConfiguration"){
    val result = new GoPluginDnsPoller().validateRepositoryConfiguration(Map("RAMDOM" -> "rand"), GoPluginDnsPoller.repositoryFields)
    println(result)
  }
}
