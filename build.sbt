import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "transit-movements-trader-authorization"

val silencerVersion = "1.7.3"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.13",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 9498,
    RoutesKeys.routesImport ++= Seq(
      "models.domain._"
    ),
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
//  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(itSettings))
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scoverageSettings)

lazy val scoverageSettings = Def.settings(
  parallelExecution in Test                := false,
  ScoverageKeys.coverageMinimumStmtTotal   := 90,
  ScoverageKeys.coverageMinimumBranchTotal := 90,
  ScoverageKeys.coverageFailOnMinimum      := true,
  ScoverageKeys.coverageHighlighting       := true,
  ScoverageKeys.coverageExcludedFiles := Seq(
    "<empty>",
    ".*javascript.*",
    ".*Routes.*",
    ".*handlers.*",
    ".*GuiceInjector",
    ".*ControllerConfiguration",
    ".*LanguageSwitchController"
  ).mkString(";"),
  ScoverageKeys.coverageExcludedPackages := Seq(
    """uk\.gov\.hmrc\.BuildInfo*""",
    """.*\.Routes""",
    """.*\.RoutesPrefix""",
    """.*\.Reverse[^.]*""",
    """.*testonly\.*""",
    """.*\.config.*""",
    """models*"""
  ).mkString(";")
)

lazy val testSettings = Def.settings(
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf",
    "-Dlogger.resource=logback-test.xml"
  )
)

lazy val itSettings = Def.settings(
  // Must fork so that config system properties are set
  fork := true,
//  parallelExecution := false,
  unmanagedSourceDirectories ++= Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test" / "base"
  ),
  unmanagedResourceDirectories += (baseDirectory.value / "it" / "resources"),
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf",
    "-Dlogger.resource=logback-test.xml"
  )
)
