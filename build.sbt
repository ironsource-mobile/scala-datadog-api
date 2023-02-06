
val scala12 = "2.12.17"
val scala13 = "2.13.10"
val supportedScalaVersion = Seq(scala12, scala13)

lazy val root = (project in file("."))
  .settings(
    name := "scala-datadog-api",
    organization := "com.supersonic",
    scalaVersion := scala13,
    crossScalaVersions := supportedScalaVersion,
    scalacOptions ++= List(
      "-encoding", "UTF-8",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-language:higherKinds",
      "-Xfatal-warnings",
      "-Ywarn-value-discard",
      "-Xlint"),
    sonatypeCredentialHost := Sonatype.sonatype01,
    libraryDependencies ++= (dependencies ++ testDependencies),
    Compile / doc / sources := List.empty)

sonatypeCredentialHost := Sonatype.sonatype01
inThisBuild(List(
  organization := "com.supersonic",
  homepage := Some(url("https://github.com/SupersonicAds/scala-datadog-api")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(Developer("SupersonicAds", "SupersonicAds", "SupersonicAds", url("https://github.com/SupersonicAds"))),
  scmInfo := Some(ScmInfo(url("https://github.com/SupersonicAds/scala-datadog-api"), "scm:git:git@github.com:SupersonicAds/scala-datadog-api.git")),

  githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
  githubWorkflowTargetTags ++= Seq("v*"),
  githubWorkflowScalaVersions := supportedScalaVersion,
  githubWorkflowPublish := Seq(
    WorkflowStep.Sbt(
      List("ci-release"),
      env = Map(
        "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
        "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
        "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
        "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}")))))

def dependencies = List(
  "com.beachape" %% "enumeratum" % "1.5.13", //TODO consider removing
  "com.softwaremill.sttp" %% "core" % "1.7.2",
  "com.softwaremill.sttp" %% "circe" % "1.7.2"
) ++ circeDependencies

def testDependencies = List(
  "org.scalatest" %% "scalatest" % "3.2.15" % "test")

def circeDependencies = {
  val circeVersion = "0.12.1"

  List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
}
