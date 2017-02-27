package fr.ramiro.gocd.dns.poller

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest
import com.thoughtworks.go.plugin.api.response.{DefaultGoPluginApiResponse, GoPluginApiResponse}
import com.thoughtworks.go.plugin.api.{GoApplicationAccessor, GoPlugin, GoPluginIdentifier}
import GoPluginDnsPoller._
import DefaultGoPluginApiResponse._
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._


object GoPluginDnsPoller{
  val DNS_SERVER = RepositoryConfigurationField("DNS_SERVER", "DNS Server", 0)
  val DNS_RECORD = PackageConfigurationField("DNS_RECORD", "DNS Record", 0, "www.google.com")
  val repositoryFields: Seq[RepositoryConfigurationField] = Seq(DNS_SERVER)
  val packageFields: Seq[PackageConfigurationField] = Seq(DNS_RECORD)
}

class GoPluginDnsPoller extends GoPlugin with GoPluginWritersAndReaders {

  override def handle(requestMessage: GoPluginApiRequest): GoPluginApiResponse = {
    requestMessage.requestName match {
      case "repository-configuration" => Some(success(compact(asJValue(repositoryFields))))
      case "package-configuration" => Some(success(compact(asJValue(packageFields))))
      case "validate-repository-configuration" => Some(success(compact(asJValue(
          validateRepositoryConfiguration(
            toConfigurationMap(requestMessage.requestBody, "repository-configuration"), repositoryFields
          ).toArray
        ))))
      case "validate-package-configuration" => Some(success(compact(asJValue(
        validatePackageConfiguration(
          toConfigurationMap(requestMessage.requestBody, "repository-configuration"), repositoryFields,
          toConfigurationMap(requestMessage.requestBody, "package-configuration"), packageFields
        ).toArray
      ))))
      case "check-repository-connection" => None
      case "check-package-connection" => None
      case "latest-revision" => None
      case "latest-revision-since" => None
      case _ => None
    }
  }.orNull

  def validateRepositoryConfiguration(repositoryMap: Map[String,String], repositoryFields: Seq[RepositoryConfigurationField]): Seq[ValidationError] = {
    val allowedKeys = repositoryFields.map{ _.name }
    (repositoryMap.keys.toSeq diff allowedKeys).map { unknownKey =>
      ValidationError(unknownKey, s"Unsupported key found : $unknownKey. Allowed key(s) are : ${allowedKeys.mkString(", ")}")
    }
  }

  def validatePackageConfiguration(
    repositoryMap: Map[String,String],
    repositoryFields: Seq[RepositoryConfigurationField],
    packageMap: Map[String,String],
    packageFields: Seq[PackageConfigurationField]
  ): Seq[ValidationError] = {
    val allowedKeys = packageFields.map{ _.name }
    validateRepositoryConfiguration(repositoryMap, repositoryFields) ++ (packageMap.keys.toSeq diff allowedKeys).map { unknownKey =>
      ValidationError(unknownKey, s"Unsupported key found : $unknownKey. Allowed key(s) are : ${allowedKeys.mkString(", ")}")
    }
  }

  private def toConfigurationMap(requestBody: String, label: String) = {
    (parse(StringInput(requestBody)) \ label).values.asInstanceOf[Map[String, Map[String, String]]].map{ case (key, property) => key -> property("value") }
  }

  override def pluginIdentifier(): GoPluginIdentifier = {
    import scala.collection.JavaConverters._
    new GoPluginIdentifier("GoPluginDnsPoller", Seq("17.0").asJava)
  }

  override def initializeGoApplicationAccessor(goApplicationAccessor: GoApplicationAccessor): Unit = {}
}
