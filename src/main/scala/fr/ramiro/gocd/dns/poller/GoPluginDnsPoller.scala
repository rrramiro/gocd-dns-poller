package fr.ramiro.gocd.dns.poller

import java.net.InetAddress
import java.util.Date
import com.thoughtworks.go.plugin.api.GoApplicationAccessor
import com.thoughtworks.go.plugin.api.annotation.Extension
import fr.ramiro.gocd.plugins._

@Extension
class GoPluginDnsPoller extends PollerPlugin[DnsServerConfig, DnsRecordConfig]("GoPluginDnsPoller", "1.0") {

  override def validateRepositoryConfiguration(repositoryConfig: DnsServerConfig): Seq[ValidationError] = {
    if (repositoryConfig.dnsServer.isEmpty) {
      Seq(ValidationError("DNS_SERVER", "Dns Server shouldn't be empty"))
    } else {
      Seq.empty
    }
  }

  override def validatePackageConfiguration(repositoryConfig: DnsServerConfig, packageConfig: DnsRecordConfig): Seq[ValidationError] = {
    if (packageConfig.dnsRecord.isEmpty) {
      Seq(ValidationError("DNS_RECORD", "Dns Record shouldn't be empty"))
    } else {
      Seq.empty
    }
  }

  override def checkRepositoryConnection(repositoryConfig: DnsServerConfig): StatusResponse = dnsResolve("www.google.com")

  override def checkPackageConnection(repositoryConfig: DnsServerConfig, packageConfig: DnsRecordConfig): StatusResponse = dnsResolve(packageConfig.dnsRecord)

  private def dnsResolve(domain: String) = {
    try {
      StatusResponse(status = true, Seq(s"Lookup $domain: ${InetAddress.getAllByName(domain).map { _.getHostAddress }.mkString(", ")}"))
    } catch {
      case t: Throwable =>
        StatusResponse(status = false, Seq(t.getMessage))
    }
  }

  override def initializeGoApplicationAccessor(goApplicationAccessor: GoApplicationAccessor): Unit = {}

  override def latestRevision(repositoryConfig: DnsServerConfig, packageConfig: DnsRecordConfig): PackageRevision = PackageRevision("1.0", new Date)

  override def latestRevisionSince(repositoryConfig: DnsServerConfig, packageConfig: DnsRecordConfig, previousRevision: PackageRevision): PackageRevision = PackageRevision("1.0", new Date)
}
