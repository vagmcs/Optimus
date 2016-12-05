import sbt.Keys._

sonatypeProfileName := "com.github.vagmcs"

lazy val root = project.in(file(".")).
  aggregate(core, oj, lpsolve, gurobi, mosek).
  settings(Seq(
    name := "optimus-assembly",
    publish := { },
    publishLocal := { }
  ))

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
lazy val gurobi =
if(file("lib/gurobi.jar").exists)
  project.in(file("solver-gurobi"))
    .dependsOn(core % "compile->compile ; test->test")
    .settings(OptimusBuild.settings)
    .enablePlugins(JavaAppPackaging)
    .settings(Seq(
      name := "optimus-solver-gurobi",
      unmanagedJars in Compile += file("lib/gurobi.jar"),
      libraryDependencies += Dependencies.scalaTest
    ))
else {
  project.in(file("solver-gurobi"))
    .settings({
    println(s"[warn] Building without the support of Gurobi solver " +
      s"('gurobi.jar' is missing from 'lib' directory)")

    Seq(
      name := "optimus-solver-gurobi",
      publish := { },
      publishLocal := { }
    )
  })
}

// Build settings for Optimus mosek solver
lazy val mosek =
  if(file("lib/mosek.jar").exists)
    project.in(file("solver-mosek"))
      .dependsOn(core % "compile->compile ; test->test")
      .settings(OptimusBuild.settings)
      .enablePlugins(JavaAppPackaging)
      .settings(Seq(
        name := "optimus-solver-mosek",
        unmanagedJars in Compile += file("lib/mosek.jar"),
        libraryDependencies += Dependencies.scalaTest
      ))
  else {
    project.in(file("solver-mosek"))
      .settings({
        println(s"[warn] Building without the support of Mosek solver " +
          s"('mosek.jar' is missing from 'lib' directory)")

        Seq(
          name := "optimus-solver-mosek",
          publish := { },
          publishLocal := { }
        )
      })
  }