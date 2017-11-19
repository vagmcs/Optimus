import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object OptimusBuild extends AutoPlugin {

  private val logger = ConsoleLogger()

  override def requires = JvmPlugin

  /**
    * Allow the plug-in to be included automatically
    */
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = settings

  private val javaVersion: Double = sys.props("java.specification.version").toDouble

  lazy val settings: Seq[Setting[_]] = {

    logger.info(s"Loading settings for Java $javaVersion or higher.")

    commonSettings ++ jdkSettings
  }

  private val commonSettings: Seq[Setting[_]] = Seq(

    name := "Optimus",

    organization := "com.github.vagmcs",

    description := "Optimus is a mathematical programming library for Scala",

    scalaVersion := "2.12.4",

    crossScalaVersions := Seq("2.12.4", "2.11.12"),

    autoScalaLibrary := true,

    managedScalaInstance := true,

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false },

    // fork a new JVM for 'run' and 'test:run'
    //fork := true,

    // fork a new JVM for 'test:run', but not 'run'
    //fork in Test := true,

    // add a JVM option to use when forking a JVM for 'run'
    javaOptions += "-Xmx2G",

    resolvers ++= Seq(
      Resolver.mavenLocal,
      "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
      "sonatype-oss-public" at "https://oss.sonatype.org/content/groups/public/"),

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    // Information required in order to sync in Maven Central
    pomExtra :=
      <url>https://github.com/vagmcs</url>
      <licenses>
        <license>
          <name>GNU Lesser General Public License v3.0</name>
          <url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/vagmcs/Optimus.git</connection>
        <developerConnection>scm:git:git@github.com:vagmcs/Optimus.git</developerConnection>
        <url>github.com/vagmcs/Optimus</url>
      </scm>
      <developers>
        <developer>
          <id>vagmcs</id>
          <name>Evangelos Michelioudakis</name>
          <url>http://users.iit.demokritos.gr/~vagmcs/</url>
        </developer>
        <developer>
          <id>anskarl</id>
          <name>Anastasios Skarlatidis</name>
          <url>http://users.iit.demokritos.gr/~anskarl/</url>
        </developer>
      </developers>
  )

  private lazy val jdkSettings: Seq[Setting[_]] = Seq(

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),

    scalacOptions := {
      scalaBinaryVersion.value match {

        case "2.11" =>
          // Scala compiler settings for Scala 2.11.x
          Seq(
            "-deprecation",       // Emit warning and location for usages of deprecated APIs.
            "-unchecked",         // Enable additional warnings where generated code depends on assumptions.
            "-feature",           // Emit warning and location for usages of features that should be imported explicitly.
            "-target:jvm-1.8",    // Target JVM version 1.8
            "-Ywarn-dead-code",   // Warn when dead code is identified.
            "-Yinline-warnings",  // Emit inlining warnings
            "-Yclosure-elim",     // Perform closure elimination
            "-Ybackend:GenBCode"  // Use the new optimisation level
          )

        case "2.12" =>
          // Scala compiler settings for Scala 2.12.x+
          Seq(
            "-deprecation",       // Emit warning and location for usages of deprecated APIs.
            "-unchecked",         // Enable additional warnings where generated code depends on assumptions.
            "-feature",           // Emit warning and location for usages of features that should be imported explicitly.
            "-target:jvm-1.8",    // Target JVM version 1.8
            "-Ywarn-dead-code"    // Warn when dead code is identified.
          )
        case _ => sys.error(s"Unsupported version of Scala '${scalaBinaryVersion.value}'")
      }
    }
  )

}