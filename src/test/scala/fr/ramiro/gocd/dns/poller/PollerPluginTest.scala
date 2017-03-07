package fr.ramiro.gocd.dns.poller

import java.util.Date
import com.thoughtworks.go.plugin.api.GoApplicationAccessor
import fr.ramiro.gocd.plugins._
import org.scalatest.FunSuite
import org.json4s.jackson.JsonMethods.{ asJValue, pretty }
import scala.annotation.meta.field

class PollerPluginTest extends FunSuite {
  test("validateRepositoryConfiguration") {
    val jsonString =
      """{
        |    "repository-configuration": {
        |        "REPO_URL": {
        |            "value": "http://localhost.com"
        |        },
        |        "USERNAME": {
        |            "value": "user"
        |        },
        |        "PASSWORD": {
        |            "value": "password"
        |        }
        |    },
        |    "package-configuration": {
        |        "PACKAGE_SPEC": {
        |            "value": "sample-package-1.0"
        |        }
        |    }
        |}
      """.stripMargin
    assert(plugin.toPackageConfig(jsonString).packageSpec === "sample-package-1.0")
    assert(plugin.toRepositoryConfig(jsonString).repoUrl === "http://localhost.com")
    assert(plugin.toRepositoryConfig(jsonString).username === "user")
    assert(plugin.toRepositoryConfig(jsonString).password === "password")
  }

  test("GoField") {
    val annotations = PollerPlugin.listGoFields(classOf[ConfigurationObject]).toMap
    assert(annotations("field1").name() === "FIELD_1")
    assert(annotations("field1").displayName() === "Field 1")
    assert(annotations("field1").displayOrder() === 0)
    assert(annotations("field2").name() === "FIELD_2")
    assert(annotations("field2").displayName() === "Field 2")
    assert(annotations("field2").displayOrder() === 1)
  }

  test("to JSON without default value") {
    val annotations = PollerPlugin.listGoFields(classOf[RepoConfig]).toMap
    val goField = annotations("repoUrl")
    assert(pretty(asJValue(Seq(goField))) ===
      """{
        |  "REPO_URL" : {
        |    "display-name" : "Repo URL",
        |    "display-order" : 0,
        |    "required" : true,
        |    "part-of-identity" : true
        |  }
        |}""".stripMargin)
  }

  test("to JSON with default value") {
    val annotations = PollerPlugin.listGoFields(classOf[PackConfig]).toMap
    val goField = annotations("packageSpec")
    assert(pretty(asJValue(Seq(goField))) ===
      """{
      |  "PACKAGE_SPEC" : {
      |    "display-name" : "Package Spec",
      |    "display-order" : 0,
      |    "required" : true,
      |    "part-of-identity" : true,
      |    "default-value" : "someValue"
      |  }
      |}""".stripMargin)
  }

  case class RepoConfig(
    @(GoField @field)(name = "REPO_URL", displayName = "Repo URL") repoUrl: String,
    @(GoField @field)(name = "USERNAME", displayName = "Username") username: String,
    @(GoField @field)(name = "PASSWORD", displayName = "Password") password: String
  )

  case class PackConfig(@(GoField @field)(name = "PACKAGE_SPEC", displayName = "Package Spec", defaultValue = "someValue") packageSpec: String)

  case class ConfigurationObject(
    @(GoField @field)(name = "FIELD_1", displayName = "Field 1") field1: String = "test",
    @(GoField @field)(name = "FIELD_2", displayName = "Field 2", displayOrder = 1) field2: Int = 1
  )

  val plugin = new PollerPlugin[RepoConfig, PackConfig]("MockPlugin", Seq("1.0"), classOf[RepoConfig], classOf[PackConfig]) {
    override def initializeGoApplicationAccessor(goApplicationAccessor: GoApplicationAccessor): Unit = {}
    override def validateRepositoryConfiguration(repositoryConfig: RepoConfig): Seq[ValidationError] = Seq.empty
    override def validatePackageConfiguration(repositoryConfig: RepoConfig, packageConfig: PackConfig): Seq[ValidationError] = Seq.empty
    override def checkRepositoryConnection(repositoryConfig: RepoConfig): StatusResponse = StatusResponse(status = true, Seq.empty)
    override def checkPackageConnection(repositoryConfig: RepoConfig, packageConfig: PackConfig): StatusResponse = StatusResponse(status = true, Seq.empty)
    override def latestRevision(repositoryConfig: RepoConfig, packageConfig: PackConfig): PackageRevision = PackageRevision("1.0", new Date)
    override def latestRevisionSince(repositoryConfig: RepoConfig, packageConfig: PackConfig, previousRevision: PackageRevision): PackageRevision = PackageRevision("1.0", new Date)
  }
}
