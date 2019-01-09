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

object Dependencies {

  final val LogbackVersion = "1.2.3"
  final val ScalaLogging = "3.9.0"
  final val ScalaTestVersion = "3.0.5"
  final val ScalaCheckVersion = "1.13.4"
  final val LpSolveVersion = "5.5.2.0"
  final val ojAlgorithmsVersion = "47.0.0"
  final val troveVersion = "3.1.0"
  final val scalaXML = "1.0.6"
  final val enumVersion = "1.5.13"

  // Logging using slf4j and logback
  lazy val Logging: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % LogbackVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % ScalaLogging
  )

  // ScalaTest and ScalaCheck for UNIT testing
  lazy val ScalaTest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
    "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % "test"
  )

  // GNU Trove collections and other tools
  lazy val Tools: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-xml" % scalaXML,
    "net.sf.trove4j" % "core" % troveVersion,
    "com.beachape" %% "enumeratum" % enumVersion
  )

  // LpSolve library for linear programming
  lazy val LpSolve: ModuleID = "com.datumbox" % "lpsolve" % LpSolveVersion

  // oj! Algorithms library for linear and quadratic programming
  lazy val ojAlgorithms: ModuleID = "org.ojalgo" % "ojalgo" % ojAlgorithmsVersion
}