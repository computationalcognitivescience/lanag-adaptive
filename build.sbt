val username = "computationalcognitivescience"
val repo = "lanag-adaptive"

updateOptions := updateOptions.value.withCachedResolution(false)

lazy val commonSettings = Seq(
  name := repo,
  scalaVersion := "2.12.8",
  organization := s"com.markblokpoel",
  description := "This is an implementation of adaptive agents for the Lanag agent-based simulation framework.",
  crossScalaVersions := Seq("2.12.8"),
  crossVersion := CrossVersion.binary,
  resolvers ++= Seq(
    "jitpack" at "https://jitpack.io"
  ),
  libraryDependencies += Dependencies.scalatest,
  libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.4.1" % Provided,
    "org.apache.spark" %% "spark-sql" % "2.4.1" % Provided,
    "com.typesafe" % "config" % "1.3.3",
    "com.markblokpoel" %% "lanag-core" % "0.3.8"
  ),
  // Compile options
  updateImpactOpenBrowser := false,
  compile in Compile := (compile in Compile).dependsOn(formatAll).value,
  mainClass in assembly := Some("com.markblokpoel.lanag.core.util.DefaultMain"),
  test in Test := (test in Test).dependsOn(checkFormat).value,
  formatAll := {
    (scalafmt in Compile).value
    (scalafmt in Test).value
    (scalafmtSbt in Compile).value
  },
  checkFormat := {
    (scalafmtCheck in Compile).value
    (scalafmtCheck in Test).value
    (scalafmtSbtCheck in Compile).value
  }
)

lazy val root = (project in file("."))
  .settings(name := s"$repo")
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(docSettings: _*)
  .enablePlugins(SiteScaladocPlugin)
  .enablePlugins(GhpagesPlugin)
  .dependsOn(probability4scala)


lazy val probability4scala = RootProject(
  uri("https://github.com/markblokpoel/probability4scala.git#master")
)

/*
 Scaladoc settings
 Note: To compile diagrams, Graphviz must be installed in /usr/local/bin
 */
import com.typesafe.sbt.SbtGit.GitKeys._
lazy val docSettings = Seq(
  autoAPIMappings := true,
  siteSourceDirectory := target.value / "api",
  git.remoteRepo := scmInfo.value.get.connection,
  envVars in ghpagesPushSite += ("SBT_GHPAGES_COMMIT_MESSAGE" -> s"Publishing Scaladoc [CI SKIP]"),
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-diagrams",
    "-implicits",
    "-doc-root-content",
    baseDirectory.value + "/overview.txt",
    "-doc-title",
    "Language Agents",
    "-diagrams-dot-path",
    "/usr/local/bin/dot"
  )
)

// Enforce source formatting before submit
lazy val formatAll = taskKey[Unit](
  "Format all the source code which includes src, test, and build files")
lazy val checkFormat = taskKey[Unit](
  "Check all the source code which includes src, test, and build files")

// Github and OSS Sonatype/Maven publish settings
lazy val publishSettings = Seq(
  homepage := Some(url(s"https://github.com/$username/$repo")),
  licenses += "GPLv3" -> url(
    s"https://github.com/$username/$repo/blob/master/LICENSE"),
  scmInfo := Some(
    ScmInfo(url(s"https://github.com/$username/$repo"),
            s"git@github.com:$username/$repo.git")),
  apiURL := Some(url(s"https://$username.github.io/$repo/latest/api/")),
)
