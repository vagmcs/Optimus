import sbt.Keys._
import sbt._

object OptimusBuild {

  val javaVersion = sys.props("java.specification.version").toDouble

  lazy val settings: Seq[Setting[_]] = {

    println(s"[info] Loading settings for Java $javaVersion or higher.")
    jdkSettings
  }

  private val commonSettings: Seq[Setting[_]] = Seq(

    name := "Optimus",

    version := "1.2.1",

    organization := "com.github.vagm",

    scalaVersion := "2.11.7",

    autoScalaLibrary := true,

    managedScalaInstance := true,

    // fork a new JVM for 'run' and 'test:run'
    fork := true,

    // fork a new JVM for 'test:run', but not 'run'
    fork in Test := true,

    // add a JVM option to use when forking a JVM for 'run'
    javaOptions += "-Xmx2G"
  )

  private lazy val jdkSettings: Seq[Setting[_]] = commonSettings ++ Seq(

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