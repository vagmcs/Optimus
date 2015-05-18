name := "Optimus"

version := "1.2"

organization := "com.github.vagm"

scalaVersion := "2.11.6"

autoScalaLibrary := true

managedScalaInstance := true

// Append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation")

// Append scalac options
scalacOptions ++= Seq(
  "-Yclosure-elim",
  "-Yinline",
  "-feature",
  "-target:jvm-1.8",
  "-language:implicitConversions",
  "-Ybackend:GenBCode"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// fork a new JVM for 'test:run', but not 'run'
fork in Test := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx2G"

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-oss-public" at "https://oss.sonatype.org/content/groups/public/"
)

// Scala-lang
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % scalaVersion.value,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

// Dependencies for unit testing (only for compile and test, exclude from publishing)
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Trove Collections
libraryDependencies += "net.sf.trove4j" % "trove4j" % "3.0.3"

libraryDependencies += "org.ojalgo" % "ojalgo" % "38.0" from "https://repo1.maven.org/maven2/org/ojalgo/ojalgo/38.0/ojalgo-38.0.jar"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// Optionally build for LP Solve and Gurobi solvers
excludeFilter := {
  var excludeNames = Set[String]()
  try {
    val jars = unmanagedBase.value.list().map(_.toLowerCase).filter(_.endsWith(".jar"))
    if (!jars.contains("gurobi.jar")) {
      println(s"[warn] Will build without the support of Gurobi solver ('gurobi.jar' is missing from '${unmanagedBase.value.getName}' directory)")
      excludeNames += "Gurobi"
    }
    if (!jars.contains("lpsolve55j.jar")) {
      println(s"[warn] Will build without the support of LP-solver ('lpsolve55j.jar' is missing from '${unmanagedBase.value.getName}' directory)")
      excludeNames += "LPSolve"
    }
    excludeNames.map(n => new SimpleFileFilter(_.getName.contains(n)).asInstanceOf[FileFilter]).reduceRight(_ || _)
  } catch{
    case _ : Exception => new SimpleFileFilter(_ => false)
  }
}
