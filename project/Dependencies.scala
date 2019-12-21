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

  object v {
    final val Logback = "1.2.3"
    final val ScalaLogging = "3.9.2"

    final val ScalaTest = "3.0.8"
    final val ScalaCheck = "1.14.0"

    final val LpSolve = "5.5.2.0"
    final val ojAlgorithms = "47.0.0"
    final val Trove = "3.1.0"
    final val ScalaXML = "1.2.0"
    final val Enums = "1.5.14"
  }

  // Logging using slf4j and logback
  lazy val Logging: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % v.Logback,
    "com.typesafe.scala-logging" %% "scala-logging" % v.ScalaLogging
  )

  // ScalaTest and ScalaCheck for UNIT testing
  lazy val ScalaTest: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % v.ScalaTest % "test",
    "org.scalacheck" %% "scalacheck" % v.ScalaCheck % "test"
  )

  // GNU Trove collections and other tools
  lazy val Tools: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-xml" % v.ScalaXML,
    "net.sf.trove4j" % "core" % v.Trove,
    "com.beachape" %% "enumeratum" % v.Enums
  )

  // LpSolve library for linear programming
  lazy val LpSolve: ModuleID = "com.datumbox" % "lpsolve" % v.LpSolve

  // oj! Algorithms library for linear and quadratic programming
  lazy val ojAlgorithms: ModuleID = "org.ojalgo" % "ojalgo" % v.ojAlgorithms
}