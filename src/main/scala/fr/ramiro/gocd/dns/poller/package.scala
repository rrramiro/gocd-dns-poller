package fr.ramiro.gocd.dns

import fr.ramiro.gocd.plugins.{GoField, PluginConfig}

import scala.annotation.meta.field

package object poller {
  case class DnsServerConfig(@(GoField @field)("DNS_SERVER", "DNS Server") dnsServer: String) extends PluginConfig

  case class DnsRecordConfig(@(GoField @field)("DNS_RECORD", "DNS Record", defaultValue = Some("www.google.com")) dnsRecord: String) extends PluginConfig

}
