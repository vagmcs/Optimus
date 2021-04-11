## Create a Model Specification

Import the following optimization packages:

```scala
import optimus.optimization._
import optimus.optimization.enums.SolverLib
import optimus.optimization.model.MPFloatVar
```

Extend your object or class using ``ModelSpec``:

```scala
object Problem extends ModelSpec(SolverLib.oJSolver) with App {

  val x = MPFloatVar(100, 200)
  val y = MPFloatVar(80, 170)

  maximize(-2 * x + 5 * y)
  add(y >:= -x + 200)

  start()

  release()
}
```