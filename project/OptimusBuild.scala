import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import scoverage.ScoverageKeys._

object OptimusBuild extends AutoPlugin {

  private val logger = ConsoleLogger()

  override def requires: Plugins = JvmPlugin

  // Allow the plug-in to be included automatically
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = settings

  private val javaVersion: Double = sys.props("java.specification.version").toDouble

  private lazy val settings: Seq[Setting[_]] = {
    logger.info(s"Loading options for Java $javaVersion.")
    if (javaVersion < 11) sys.error("Java 11 or higher is required for running Optimus.")
    else commonSettings ++ JavaSettings ++ ScalaSettings
  }

  private val commonSettings: Seq[Setting[_]] = Seq(
    organization := "com.github.vagmcs",
    description := "Optimus is a mathematical programming library for Scala",
    scalaVersion := "3.3.4",
    crossScalaVersions := Seq(scalaVersion.value, "2.13.15", "2.12.20"),
    autoScalaLibrary := true,
    managedScalaInstance := true,
    coverageEnabled := false,
    coverageHighlighting := true,
    coverageMinimumStmtTotal := 75,
    publishMavenStyle := true,
    Test / publishArtifact := false,
    pomIncludeRepository := { _ => false },
    resolvers ++= Seq(
      Seq(
        Resolver.mavenLocal,
        Resolver.typesafeRepo("releases")
      ),
      Resolver.sonatypeOssRepos("releases"),
      Resolver.sonatypeOssRepos("snapshots")
    ).flatten,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    scmInfo := Some(
      ScmInfo(url("https://github.com/vagmcs/Optimus"), "scm:git:git@github.com:vagmcs/Optimus.git")
    ),

    // Information required in order to sync in Maven Central
    pomExtra :=
      <url>https://github.com/vagmcs</url>
      <licenses>
        <license>
          <name>GNU Lesser General Public License v3.0</name>
          <url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
        </license>
      </licenses>
      <developers>
        <developer>
          <id>vagmcs</id>
          <name>Evangelos Michelioudakis</name>
          <url>https://users.iit.demokritos.gr/~vagmcs/</url>
        </developer>
        <developer>
          <id>anskarl</id>
          <name>Anastasios Skarlatidis</name>
          <url>https://users.iit.demokritos.gr/~anskarl/</url>
        </developer>
      </developers>
  )

  private lazy val JavaSettings: Seq[Setting[_]] = Seq(
    // Java runtime options
    javaOptions ++= Seq(
      "-XX:+DoEscapeAnalysis",
      "-XX:+OptimizeStringConcat",
      "-Dlogback.configurationFile=src/main/resources/logback.xml"
    )
  )

  private lazy val ScalaSettings: Seq[Setting[_]] = Seq(
    scalacOptions := {
      scalaBinaryVersion.value match {

        case "2.12" | "2.13" =>
          // Scala compiler settings for Scala 2.12.x and 2.13.x
          Seq(
            "-deprecation", // Emit warning and location for usages of deprecated APIs.
            "-unchecked", // Enable additional warnings where generated code depends on assumptions.
            "-feature", // Emit warning and location for usages of features that should be imported explicitly.
            "-Ywarn-dead-code" // Warn when dead code is identified.
          )
        case "3" =>
          // Scala compiler settings for Scala 3.x
          Seq(
            "-deprecation", // Emit warning and location for usages of deprecated APIs.
            "-unchecked", // Enable additional warnings where generated code depends on assumptions.
            "-feature" // Emit warning and location for usages of features that should be imported explicitly.
          )
        case _ => sys.error(s"Unsupported version of Scala '${scalaBinaryVersion.value}'")
      }
    }
  )
}
