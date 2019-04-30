lazy val root = (project in file("."))
  .settings(
    name := "scala-datadog-api",
    organization := "com.supersonic",
    scalaVersion := "2.12.6",
    crossScalaVersions := List(scalaVersion.value, "2.11.12"),
    scalacOptions ++= List(
      "-encoding", "UTF-8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:higherKinds",
      "-Xfatal-warnings",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Xlint",
      "-Ypartial-unification"),
    libraryDependencies ++= (dependencies ++ testDependencies),
    sources in (Compile, doc) := List.empty)

inThisBuild(List(
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/SupersonicAds/scala-datadog-api")),
  developers := List(Developer("SupersonicAds", "SupersonicAds", "SupersonicAds", url("https://github.com/SupersonicAds"))),
  scmInfo := Some(ScmInfo(url("https://github.com/SupersonicAds/scala-datadog-api"), "scm:git:git@github.com:SupersonicAds/scala-datadog-api.git")),

  pgpPublicRing := file("./travis/local.pubring.asc"),
  pgpSecretRing := file("./travis/local.secring.asc"),
  releaseEarlyEnableSyncToMaven := false,
  releaseEarlyWith := BintrayPublisher))

def dependencies = List(
  "com.beachape" %% "enumeratum" % "1.5.13", //TODO consider removing
  "com.softwaremill.sttp" %% "core" % "1.1.14",
  "com.softwaremill.sttp" %% "circe" % "1.1.14"
) ++ circeDependencies

def testDependencies = List(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test")

def circeDependencies = {
  val circeVersion = "0.11.1"

  List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
}
