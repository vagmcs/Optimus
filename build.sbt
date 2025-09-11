addCommandAlias("check", ";headerCreate;dependencyUpdates;compile")
addCommandAlias("build", ";check;test;coverageAggregate;package")
addCommandAlias("rebuild", ";clean;build")

val logger = ConsoleLogger()

useGpgPinentry := true
sonatypeProfileName := "com.github.vagmcs"

lazy val root = project
  .in(file("."))
  .aggregate(core, oj, lpsolve, gurobi, mosek)
  .settings(publish / skip := true)

// Build settings for Optimus core
lazy val core = project
  .in(file("core"))
  .settings(Test / logLevel := Level.Info)
  .settings(name := "optimus")
  .settings(
    Seq(
      libraryDependencies ++= Dependencies.Logging,
      libraryDependencies ++= Dependencies.ScalaTest,
      libraryDependencies ++= Dependencies.Tools
    )
  )

// Build settings for Optimus oj solver
lazy val oj = Project("solver-oj", file("solver-oj"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(name := "optimus-solver-oj")
  .settings(libraryDependencies += Dependencies.ojAlgorithms)

// Build settings for Optimus lp solver
lazy val lpsolve = Project("solver-lp", file("solver-lp"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(name := "optimus-solver-lp")
  .settings(libraryDependencies += Dependencies.LpSolve)

// Build settings for Optimus gurobi solver
lazy val gurobi = Project("solver-gurobi", file("solver-gurobi"))
  .dependsOn(core % "compile->compile ; test->test")
  .settings(name := "optimus-solver-gurobi")
  .settings(libraryDependencies += Dependencies.Gurobi)

// Build settings for Optimus mosek solver
lazy val mosek =
  if (file("lib/mosek.jar").exists) Project("solver-mosek", file("solver-mosek"))
    .dependsOn(core % "compile->compile ; test->test")
    .settings(name := "optimus-solver-mosek")
    .settings(Compile / unmanagedJars += file("lib/mosek.jar"))
  else Project("solver-mosek", file("solver-mosek"))
    .settings {
      logger.warn {
        "Building in the absence of support for the Mosek solver [ 'mosek.jar' not found in 'lib' directory ]."
      }
      Seq(name := "optimus-solver-mosek", publish := {}, publishLocal := {})
    }
