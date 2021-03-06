package fr.ramiro.gocd.dns

import fr.ramiro.gocd.plugins.GoField
import scala.annotation.meta.field

package object poller {

  case class DnsServerConfig(
    @(GoField @field)(
      name = "DNS_SERVER",
      displayName = "DNS Server"
    ) dnsServer: String
  )

  case class DnsRecordConfig(
    @(GoField @field)(
      name = "DNS_RECORD",
      displayName = "DNS Record",
      defaultValue = "www.google.com"
    ) dnsRecord: String
  )

}
