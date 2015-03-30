import com.typesafe.sbt.SbtNativePackager._

name := "Optimus"

version := "1.1"

organization := "com.github.vagm"

scalaVersion := "2.11.6"

autoScalaLibrary := true

managedScalaInstance := true

packageArchetype.java_application

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

// Optimized Range foreach loops
//libraryDependencies += "com.nativelibs4java" %% "scalaxy-streams" % "0.3.4" % "provided"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
