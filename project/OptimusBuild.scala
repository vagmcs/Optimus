/*
 *
 *   /\\\\\
 *  /\\\///\\\
 * /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 * /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 * \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *  \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *    \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *       \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *          \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import de.heikoseeberger.sbtheader.{FileType, CommentStyle}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._

object OptimusBuild extends AutoPlugin {

  private val logger = ConsoleLogger()

  println {
    """
      |   /\\\\\
      |  /\\\///\\\
      | /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
      | /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
      | \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
      |  \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
      |    \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
      |       \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
      |          \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
    """.stripMargin
  }

  override def requires: Plugins =
    JvmPlugin && HeaderPlugin && AutomateHeaderPlugin

  // Allow the plug-in to be included automatically
  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = settings

  private val javaVersion: Double = sys.props("java.specification.version").toDouble

  private lazy val settings: Seq[Setting[_]] = {
    logger.info(s"Loading settings for Java $javaVersion or higher.")
    if (javaVersion < 1.8) sys.error("Java 8 or higher is required for building Optimus.")
    else commonSettings ++ JavaSettings ++ ScalaSettings ++ Reform.formatSettings
  }

  private val commonSettings: Seq[Setting[_]] = Seq(

    name := "Optimus",

    organization := "com.github.vagmcs",

    description := "Optimus is a mathematical programming library for Scala",

    headerLicense := projectHeaders,

    headerMappings := headerMappings.value + (FileType.scala -> CommentStyle.cStyleBlockComment),

    scalaVersion := "2.12.6",

    crossScalaVersions := Seq("2.12.6", "2.11.12"),

    autoScalaLibrary := true,

    managedScalaInstance := true,

    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },

    resolvers ++= Seq(
      Resolver.mavenLocal,
      Resolver.typesafeRepo("releases"),
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")
    ),

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

  private lazy val JavaSettings: Seq[Setting[_]] = Seq(

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"),

    javaOptions ++= Seq(
      "-XX:+DoEscapeAnalysis",
      "-XX:+UseFastAccessorMethods",
      "-XX:+OptimizeStringConcat",
      "-Dlogback.configurationFile=src/main/resources/logback.xml")
  )

  private lazy val ScalaSettings: Seq[Setting[_]] = Seq(
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
          // Scala compiler settings for Scala 2.12.x
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

  lazy val projectHeaders = Some(
    HeaderLicense.Custom(
      """
        |  /\\\\\
        | /\\\///\\\
        |/\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
        |/\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
        |\/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
        | \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
        |   \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
        |      \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
        |         \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
        |
        |Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
        |
        |Optimus is free software: you can redistribute it and/or modify it
        |under the terms of the GNU Lesser General Public License as published
        |by the Free Software Foundation, either version 3 of the License,
        |or (at your option) any later version.
        |
        |Optimus is distributed in the hope that it will be useful, but WITHOUT
        |ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
        |or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
        |License for more details.
        |
        |You should have received a copy of the GNU Lesser General Public License
        |along with Optimus. If not, see <http://www.gnu.org/licenses/>.
      """.stripMargin
    )
  )
}