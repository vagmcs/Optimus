import sbt.Keys._

addCommandAlias("build", ";headerCreate;compile;test;package")
addCommandAlias("rebuild", ";clean;build")

val logger = ConsoleLogger()

sonatypeProfileName := "com.github.vagmcs"

useGpg := true

lazy val root = project.in(file("."))
  .aggregate(core, oj, lpsolve, gurobi, mosek)
  .settings(publish := { }, publishLocal := { })

publishArtifact in root := false

// Build settings for Optimus core
val core = project.in(file("core"))
  .enablePlugins(AutomateHeaderPlugin)
  .settings(logLevel in Test := Level.Info)
  .settings(logLevel in Compile := Level.Error)
  .settings(name := "optimus")
  .settings(Seq(
    libraryDependencies ++= Dependencies.Logging,
    libraryDependencies ++= Dependencies.ScalaTest,
    libraryDependencies ++= Dependencies.Tools
  ))

// Build settings for Optimus oj solver
val oj = Project("solver-oj", file("solver-oj"))
  .dependsOn(core % "compile->compile ; test->test")
  .enablePlugins(AutomateHeaderPlugin)
  .settings(name := "optimus-solver-oj")
  .settings(libraryDependencies += Dependencies.ojAlgorithms)

// Build settings for Optimus lp solver
val lpsolve = Project("solver-lp", file("solver-lp"))
  .dependsOn(core % "compile->compile ; test->test")
  .enablePlugins(AutomateHeaderPlugin)
  .settings(name := "optimus-solver-lp")
  .settings(libraryDependencies += Dependencies.LpSolve)

// Build settings for Optimus gurobi solver
val gurobi = if (file("lib/gurobi.jar").exists)
  Project("solver-gurobi", file("solver-gurobi"))
    .dependsOn(core % "compile->compile ; test->test")
    .enablePlugins(AutomateHeaderPlugin)
    .settings(name := "optimus-solver-gurobi")
    .settings(unmanagedJars in Compile += file("lib/gurobi.jar"))
else
  Project("solver-gurobi", file("solver-gurobi"))
    .settings({
      logger.warn {
        "Building in the absence of support for the Gurobi solver [ 'gurobi.jar' not found in 'lib' directory ]."}
      Seq(name := "optimus-solver-gurobi", publish := { }, publishLocal := { })
    })

// Build settings for Optimus mosek solver
val mosek = if (file("lib/mosek.jar").exists)
    Project("solver-mosek", file("solver-mosek"))
      .dependsOn(core % "compile->compile ; test->test")
      .enablePlugins(AutomateHeaderPlugin)
      .settings(name := "optimus-solver-mosek")
      .settings(unmanagedJars in Compile += file("lib/mosek.jar"))
else
  Project("solver-mosek", file("solver-mosek"))
    .settings({
      logger.warn {
        "Building in the absence of support for the Mosek solver [ 'mosek.jar' not found in 'lib' directory ]."}
      Seq(name := "optimus-solver-mosek", publish := { }, publishLocal := { })
    })