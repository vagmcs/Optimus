import com.typesafe.sbt.SbtNativePackager._

name := "Optimus"

version := "1.0"

organization := "com.github.vagm"

crossScalaVersions := Seq("2.10.4", "2.11.4")

autoScalaLibrary := true

managedScalaInstance := true

packageArchetype.java_application

// Append several options to the list of options passed to the Java compiler
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

// Append scalac options
scalacOptions ++= Seq(
  "-optimise",
  "-Yclosure-elim",
  "-Yinline",
  "-feature",
  "-target:jvm-1.7",
  "-language:implicitConversions"
)

// fork a new JVM for 'run' and 'test:run'
fork := true

// fork a new JVM for 'test:run', but not 'run'
fork in Test := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions += "-Xmx1G"

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-oss-public" at "https://oss.sonatype.org/content/groups/public/"
)

// Dependencies for unit testing (only for compile and test, exclude from publishing)
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "compile-internal, test-internal"


publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
