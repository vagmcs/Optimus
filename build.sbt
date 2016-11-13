import sbt.Keys._

lazy val core = project.in(file("core"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(Seq(
    name := "optimus",
    libraryDependencies += Dependencies.scalaTest,
    // Trove Collections
    libraryDependencies += Dependencies.trove4j,
    libraryDependencies += Dependencies.scalaXml
  ))

lazy val `solver-oj` = project.in(file("solver-oj"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(Seq(
    name := "optimus-solver-oj",
    // oj algorithms library for optimization
    libraryDependencies += Dependencies.ojAlgo,
    libraryDependencies += Dependencies.scalaTest
  ))

lazy val `solver-lp` = project.in(file("solver-lp"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(Seq(
    name := "optimus-solver-lp",
    // lp solve library for optimization
    libraryDependencies += Dependencies.lpSolve,
    libraryDependencies += Dependencies.scalaTest
  ))

lazy val `solver-gurobi` = project.in(file("solver-gurobi"))
  .dependsOn(core % "compile->compile;test->test")
  .settings(commonSettings)
  .settings(Seq(
    name := "optimus-solver-gurobi",
    libraryDependencies += Dependencies.scalaTest
  ))

lazy val commonSettings = Seq(
  version := "2.0.0-SNAPSHOT",
  organization := "com.github.vagm",
  scalaVersion := "2.11.8",
  autoScalaLibrary := true,
  managedScalaInstance := true,
  // fork a new JVM for 'run' and 'test:run'
  fork := true,
  // fork a new JVM for 'test:run', but not 'run'
  fork in Test := true,
  // add a JVM option to use when forking a JVM for 'run'
  javaOptions += "-Xmx2G",
  javacOptions ++= Seq(
    "-source", "1.8", 
    "-target", "1.8", 
    "-Xlint:unchecked", 
    "-Xlint:deprecation"),
  scalacOptions ++= Seq(
    "-Yclosure-elim",
    "-Yinline",
    "-feature",
    "-target:jvm-1.8",
    "-language:implicitConversions",
    "-Ybackend:GenBCode"),
  resolvers ++= Seq(
    "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
    "sonatype-oss-public" at "https://oss.sonatype.org/content/groups/public/")
)