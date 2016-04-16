
OptimusBuild.settings

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

// oj algorithms library for optimization
libraryDependencies += "org.ojalgo" % "ojalgo" % "39.0"

// lp solve library for optimization
libraryDependencies += "com.datumbox" % "lpsolve" % "5.5.2.0"

// Dependencies override
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// Optionally build for commercial solvers
excludeFilter := {
  var excludeNames = Set[String]()
  val jars = try unmanagedBase.value.list.map(_.toLowerCase).filter(_.endsWith(".jar"))
  catch { case _ : Exception => Array[String]() }

  if (!jars.contains("gurobi.jar")) {
    println(s"[warn] Building without the support of Gurobi solver ('gurobi.jar' is missing from '${unmanagedBase.value.getName}' directory)")
    excludeNames += "Gurobi"
  }
  if(excludeNames.isEmpty) new SimpleFileFilter(_ => false)
  else excludeNames.map(n => new SimpleFileFilter(_.getName.contains(n)).asInstanceOf[FileFilter]).reduceRight(_ || _)
}
