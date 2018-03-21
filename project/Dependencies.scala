/*
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

import sbt._

object Dependencies {

  final val LogbackVersion = "1.1.7"
  final val SLF4JVersion = "1.7.21"
  final val ScalaLogging = "3.5.0"
  final val ScalaTestVersion = "3.0.4"
  final val ScalaCheckVersion = "1.13.4"
  final val LpSolveVersion = "5.5.2.0"
  final val ojAlgorithmsVersion = "44.0.0"
  final val troveVersion = "3.0.3"
  final val scalaXML = "1.0.6"
  final val enumeratumVersion = "1.5.12"

  // Logging using slf4j and logback
  lazy val Logging = Seq(
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "ch.qos.logback" % "logback-core" % LogbackVersion,
    "org.slf4j" % "slf4j-api" % SLF4JVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLogging
  )

  // ScalaTest and ScalaCheck for UNIT testing
  lazy val ScalaTest = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
    "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % "test"
  )

  // GNU Trove collections and other tools
  lazy val Tools = Seq(
    "org.scala-lang.modules" %% "scala-xml" % scalaXML,
    "net.sf.trove4j" % "trove4j" % troveVersion,
    "com.beachape" %% "enumeratum" % enumeratumVersion
  )

  // LpSolve library for linear programming
  lazy val LpSolve: ModuleID = "com.datumbox" % "lpsolve" % LpSolveVersion

  // oj! Algorithms library for linear and quadratic programming
  lazy val ojAlgorithms: ModuleID = "org.ojalgo" % "ojalgo" % ojAlgorithmsVersion
}