package fr.ramiro.gocd.dns.poller

import java.util.Date
import com.thoughtworks.go.plugin.api.GoApplicationAccessor
import fr.ramiro.gocd.plugins._
import org.scalatest.FunSuite

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

  case class RepoConfig(
    @(GoField @field)("REPO_URL", "Repo URL") repoUrl: String,
    @(GoField @field)("USERNAME", "Username") username: String,
    @(GoField @field)("PASSWORD", "Password") password: String
  )

  case class PackConfig(@(GoField @field)("PACKAGE_SPEC", "Package Spec") packageSpec: String)

  val plugin = new PollerPlugin[RepoConfig, PackConfig]("MockPlugin", "1.0") {
    override def initializeGoApplicationAccessor(goApplicationAccessor: GoApplicationAccessor): Unit = {}
    override def validateRepositoryConfiguration(repositoryConfig: RepoConfig): Seq[ValidationError] = Seq.empty
    override def validatePackageConfiguration(repositoryConfig: RepoConfig, packageConfig: PackConfig): Seq[ValidationError] = Seq.empty
    override def checkRepositoryConnection(repositoryConfig: RepoConfig): StatusResponse = StatusResponse(status = true, Seq.empty)
    override def checkPackageConnection(repositoryConfig: RepoConfig, packageConfig: PackConfig): StatusResponse = StatusResponse(status = true, Seq.empty)
    override def latestRevision(repositoryConfig: RepoConfig, packageConfig: PackConfig): PackageRevision = PackageRevision("1.0", new Date)
    override def latestRevisionSince(repositoryConfig: RepoConfig, packageConfig: PackConfig, previousRevision: PackageRevision): PackageRevision = PackageRevision("1.0", new Date)
  }
}
