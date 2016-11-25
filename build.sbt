import sbt.Keys._

sonatypeProfileName := "com.github.vagmcs"

lazy val root = project.in(file(".")).
  aggregate(core, oj, lpsolve, gurobi).
  settings (publish := { })

publishArtifact in root := false

// Build settings for Optimus core
lazy val core = project.in(file("core"))
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus",
    libraryDependencies += Dependencies.scalaTest,
    libraryDependencies += Dependencies.trove4j,
    libraryDependencies += Dependencies.scalaXml
  ))

// Build settings for Optimus oj solver
lazy val oj = project.in(file("solver-oj"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-oj",
    libraryDependencies += Dependencies.ojalgo,
    libraryDependencies += Dependencies.scalaTest
  ))

// Build settings for Optimus lp solver
lazy val lpsolve = project.in(file("solver-lp"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-lp",
    libraryDependencies += Dependencies.lpSolve,
    libraryDependencies += Dependencies.scalaTest
  ))

// Build settings for Optimus gurobi solver
lazy val gurobi = project.in(file("solver-gurobi"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(OptimusBuild.settings)
  .enablePlugins(JavaAppPackaging)
  .settings(Seq(
    name := "optimus-solver-gurobi",
    libraryDependencies += Dependencies.scalaTest
  ))