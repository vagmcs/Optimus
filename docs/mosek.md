## Use mosek 

If you need to solve Quadratic constraint quadratic programming (QCQP) problems 
you have to use the proprietary library mosek or gurobi.

For use mosek:

- Download the installation for you platform from https://www.mosek.com/downloads/
- Copy all the files from the dir for you platform (Ex. 9.0/tools/platform/linux64x86/bin) 
to the lib directory on the root of your project (easiest way with sbt)
- Add  "com.github.vagmcs" %% "optimus-solver-mosek" % "3.2.0" to the build.sbt

Import the following optimization packages:
```scala
import optimus.optimization._
import optimus.optimization.enums.SolverLib
import optimus.optimization.model.MPFloatVar
```

Extend your object or class using ``ModelSpec``:

```scala
object Test extends ModelSpec(SolverLib.Mosek) with App {
 
      val x = MPFloatVar("x", 0, 2)
      val y = MPFloatVar("y", -1, 1)
  
      minimize((x - 2) * (x - 2) + y * y)
      subjectTo(
        x * x + y * y <:= 1.0
      )
  
      start()
  
      val r = (x.value.get, y.value.get, objectiveValue)
  
      release()
    
}
```