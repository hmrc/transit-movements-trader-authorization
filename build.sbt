import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import scoverage.ScoverageKeys

val appName = "transit-movements-trader-authorization"

val silencerVersion = "1.7.3"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion             := 0,
    scalaVersion             := "2.12.13",
    PlayKeys.playDefaultPort := 9498,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
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
  .settings(integrationTestSettings(): _*)
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
    "testonly",
    """.*\.config.*""",
    """models*"""
  ).mkString(";")
)
