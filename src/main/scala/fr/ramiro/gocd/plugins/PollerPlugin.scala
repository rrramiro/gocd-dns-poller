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
import scala.reflect

abstract class PollerPlugin[RepositoryConfig, PackageConfig](pluginName: String, managedVersions: Seq[String], rcClazz: Class[RepositoryConfig], pcClazz: Class[PackageConfig])
    extends GoPlugin with DefaultReaders with DefaultWriters {

  private val mirror = runtimeMirror(getClass.getClassLoader)
  private val pcmf: scala.reflect.Manifest[PackageConfig] = Manifest.classType(pcClazz)
  private val rcmf: scala.reflect.Manifest[RepositoryConfig] = Manifest.classType(rcClazz)
  private val repositoryFields = GoField.listGoFields[RepositoryConfig](typeToTypeTag[RepositoryConfig](mirror.classSymbol(rcClazz).toType))
  private val packageFields = GoField.listGoFields[PackageConfig](typeToTypeTag[PackageConfig](mirror.classSymbol(pcClazz).toType))

  private def typeToTypeTag[T](tpe: Type): TypeTag[T] = {
    TypeTag(mirror, new reflect.api.TypeCreator {
      def apply[U <: reflect.api.Universe with Singleton](m: reflect.api.Mirror[U]) = {
        assert(m eq mirror, s"TypeTag[$tpe] defined in $mirror cannot be migrated to $m.")
        tpe.asInstanceOf[U#Type]
      }
    })
  }

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
        case "latest-revision" => Some(success(compact(asJValue(
          latestRevision(
            toRepositoryConfig(requestMessage.requestBody),
            toPackageConfig(requestMessage.requestBody)
          )
        ))))
        case "latest-revision-since" => Some(success(compact(asJValue(
          latestRevisionSince(
            toRepositoryConfig(requestMessage.requestBody),
            toPackageConfig(requestMessage.requestBody),
            toPreviousRevision(requestMessage.requestBody)
          )
        ))))
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

  def toRepositoryConfig(requestBody: String): RepositoryConfig = requestToJObject[RepositoryConfig](requestBody, "repository-configuration", paramsToField(repositoryFields))(rcmf)

  def toPackageConfig(requestBody: String): PackageConfig = requestToJObject[PackageConfig](requestBody, "package-configuration", paramsToField(packageFields))(pcmf)

  def toPreviousRevision(requestBody: String): PackageRevision = {
    (parse(StringInput(requestBody)) \ "previous-revision").as[PackageRevision]
  }

  def validateRepositoryConfiguration(repositoryConfig: RepositoryConfig): Seq[ValidationError]

  def validatePackageConfiguration(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): Seq[ValidationError]

  def checkRepositoryConnection(repositoryConfig: RepositoryConfig): StatusResponse

  def checkPackageConnection(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): StatusResponse

  def latestRevision(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig): PackageRevision

  def latestRevisionSince(repositoryConfig: RepositoryConfig, packageConfig: PackageConfig, previousRevision: PackageRevision): PackageRevision
}
