package fr.ramiro.gocd.plugins

import com.thoughtworks.go.plugin.api.GoPlugin
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.{error, success}
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse
import org.json4s.{DefaultReaders, DefaultWriters, Writer}
import org.json4s.jackson.JsonMethods.{asJValue, compact}

import scala.reflect.runtime.universe._
//(implicit repositoryConfigWriter: Writer[RepositoryConfig], packageConfigWriter: Writer[PackageConfig])
abstract class PollerPlugin[RepositoryConfig <: PluginConfig : TypeTag, PackageConfig <: PluginConfig : TypeTag] extends GoPlugin with DefaultReaders with DefaultWriters {
  private val repositoryFields = GoField.listGoFields[RepositoryConfig]
  private val packageFields = GoField.listGoFields[PackageConfig]

  override def handle(requestMessage: GoPluginApiRequest): GoPluginApiResponse = {
    try {
      requestMessage.requestName match {
        case "repository-configuration" => Some(success(compact(asJValue(repositoryFields.map { _._2 }))))
        case "package-configuration" => Some(success(compact(asJValue(packageFields.map { _._2 }))))
        case "validate-repository-configuration" => Some(success(compact(asJValue(
          validateRepositoryConfiguration(
            toRepositoryConfig(requestMessage.requestBody)
          ).toArray
        ))))
        case "validate-package-configuration" => Some(success(compact(asJValue(
          validatePackageConfiguration(
            toRepositoryConfig(requestMessage.requestBody),
            toPackageConfig(requestMessage.requestBody)
          ).toArray
        ))))
        case "check-repository-connection" => Some(success(compact(asJValue(
          checkRepositoryConnection(
            toRepositoryConfig(requestMessage.requestBody)
          )
        ))))
        case "check-package-connection" => Some(success(compact(asJValue(
          checkPackageConnection(
            toRepositoryConfig(requestMessage.requestBody),
            toPackageConfig(requestMessage.requestBody)
          )
        ))))
        case "latest-revision" => None
        case "latest-revision-since" => None
        case _ => None
      }
    } catch {
      case t: Throwable =>
        Some(error(t.getMessage))
    }
  }.orNull

  def toRepositoryConfig(requestBody: String): RepositoryConfig = ???

  def toPackageConfig(requestBody: String): PackageConfig = ???

  def validateRepositoryConfiguration(repositoryConfig: RepositoryConfig): Seq[ValidationError]

  def validatePackageConfiguration(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): Seq[ValidationError]

  def checkRepositoryConnection(repositoryConfig: RepositoryConfig): StatusResponse

  def checkPackageConnection(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): StatusResponse
}