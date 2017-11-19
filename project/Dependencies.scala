import sbt._

object Dependencies {

  // Dependencies for unit testing (only for compile and test, exclude from publishing)
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % "test"

  // Trove Collections
  val trove4j = "net.sf.trove4j" % "trove4j" % "3.0.3"

  val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.6"

  // oj library for optimization
  val ojalgo = "org.ojalgo" % "ojalgo" % "41.0.0"

  // lp solve library for optimization
  val lpSolve = "com.datumbox" % "lpsolve" % "5.5.2.0"
}