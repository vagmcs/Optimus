import sbt.Keys._

lazy val core = project.in(file("core"))
  .enablePlugins(JavaAppPackaging)
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus",
    libraryDependencies += Dependencies.scalaTest,
    // Trove Collections
    libraryDependencies += Dependencies.trove4j,
    libraryDependencies += Dependencies.scalaXml
  ))

lazy val `solver-oj` = project.in(file("solver-oj"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-oj",
    // oj algorithms library for optimization
    libraryDependencies += Dependencies.ojAlgo,
    libraryDependencies += Dependencies.scalaTest
  ))

lazy val `solver-lp` = project.in(file("solver-lp"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-lp",
    // lp solve library for optimization
    libraryDependencies += Dependencies.lpSolve,
    libraryDependencies += Dependencies.scalaTest
  ))

lazy val `solver-gurobi` = project.in(file("solver-gurobi"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-gurobi",
    libraryDependencies += Dependencies.scalaTest
  ))
