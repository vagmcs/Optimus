import sbt.Keys._
import sbt._

object OptimusBuild {

  val javaVersion = sys.props("java.specification.version").toDouble

  lazy val settings: Seq[Setting[_]] = {

    println(s"[info] Loading settings for Java $javaVersion or higher.")
    commonSettings ++ jdkSettings
  }

  private val commonSettings: Seq[Setting[_]] = Seq(

    name := "Optimus",

    version := "2.0.0",

    organization := "com.github.vagmcs",

    description := "Optimus is a mathematical programming library for Scala",

    scalaVersion := "2.11.8",

    autoScalaLibrary := true,

    managedScalaInstance := true,

    publishMavenStyle := true,

    publishArtifact in Test := false,

    pomIncludeRepository := { _ => false },

    // fork a new JVM for 'run' and 'test:run'
    fork := true,

    // fork a new JVM for 'test:run', but not 'run'
    fork in Test := true,

    // add a JVM option to use when forking a JVM for 'run'
    javaOptions += "-Xmx2G",

    unmanagedJars in Compile += file("lib/gurobi.jar"),

    resolvers ++= Seq(
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
    pomExtra in Global := {

      <url>https://github.com/vagmcs/Optimus</url>

        // Optimus License
        <licenses>
          <license>
            <name>GNU Lesser General Public License v3.0</name>
            <url>https://www.gnu.org/licenses/lgpl-3.0.en.html</url>
          </license>
        </licenses>

        // SCM information
        <scm>
          <connection>scm:git:github.com/vagmcs/Optimus.git</connection>
          <developerConnection>scm:git:git@github.com:vagmcs/Optimus.git</developerConnection>
          <url>github.com/vagmcs/Optimus</url>
        </scm>

        // Developer contact information
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
    }
  )

  private lazy val jdkSettings: Seq[Setting[_]] = Seq(

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),

    scalacOptions ++= Seq(
      "-Yclosure-elim",
      "-Yinline",
      "-feature",
      "-target:jvm-1.8",
      "-language:implicitConversions",
      "-Ybackend:GenBCode"
    )
  )

}