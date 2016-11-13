import sbt._

object Dependencies {
  // Dependencies for unit testing (only for compile and test, exclude from publishing)
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.6" % "test"

  val trove4j = "net.sf.trove4j" % "trove4j" % "3.0.3"

  val scalaXml = "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"

  val ojAlgo = "org.ojalgo" % "ojalgo" % "39.0"

  val lpSolve = "com.datumbox" % "lpsolve" % "5.5.2.0"
}