import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"      % "5.19.0",
    "org.reactivemongo" %% "play2-reactivemongo"            % "0.20.11-play28",
    "org.reactivemongo" %% "reactivemongo-play-json-compat" % "0.20.11-play28",
    "com.typesafe.play" %% "play-iteratees"                 % "2.6.1",
    "uk.gov.hmrc"       %% "internal-auth-client-play-27"   % "0.12.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % "5.14.0"             % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % "0.50.0"            % Test,
    "org.scalatest"          %% "scalatest"               % "3.2.5"             % Test,
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current % Test,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.36.8"            % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"             % "test, it"
  )
}
