package fr.ramiro.gocd.plugins

import com.thoughtworks.go.plugin.api.{ GoPlugin, GoPluginIdentifier }
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.{ error, success }
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse
import org.json4s.{ DefaultReaders, DefaultWriters }
import org.json4s.jackson.JsonMethods.{ asJValue, compact }
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import scala.reflect.runtime.universe._

abstract class PollerPlugin[RepositoryConfig, PackageConfig](pluginName: String, managedVersions: String*)(implicit rctt: TypeTag[RepositoryConfig], pctt: TypeTag[PackageConfig], rcmf: scala.reflect.Manifest[RepositoryConfig], pcmf: scala.reflect.Manifest[PackageConfig])
    extends GoPlugin with DefaultReaders with DefaultWriters {
  private val repositoryFields: Seq[(String, GoField)] = GoField.listGoFields[RepositoryConfig]
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

  override def pluginIdentifier(): GoPluginIdentifier = {
    import scala.collection.JavaConverters._
    new GoPluginIdentifier(pluginName, managedVersions.asJava)
  }

  private def requestToJObject[T](requestBody: String, label: String, keyToField: String => String)(implicit mf: scala.reflect.Manifest[T]): T = {
    (parse(StringInput(requestBody)) \ label)
      .as[Map[String, Map[String, String]]]
      .foldLeft(JObject()) {
        case (a, (key, property)) =>
          a ~ (keyToField(key) -> property("value"))
      }.extract[T](DefaultFormats, mf)
  }

  private def paramsToField(fields: Seq[(String, GoField)]): (String => String) = fields.map { case (field, param) => param.name -> field }.toMap

  def toRepositoryConfig(requestBody: String): RepositoryConfig = requestToJObject[RepositoryConfig](requestBody, "repository-configuration", paramsToField(repositoryFields))

  def toPackageConfig(requestBody: String): PackageConfig = requestToJObject[PackageConfig](requestBody, "package-configuration", paramsToField(packageFields))

  def validateRepositoryConfiguration(repositoryConfig: RepositoryConfig): Seq[ValidationError]

  def validatePackageConfiguration(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): Seq[ValidationError]

  def checkRepositoryConnection(repositoryConfig: RepositoryConfig): StatusResponse

  def checkPackageConnection(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): StatusResponse
}
