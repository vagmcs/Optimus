## Quadratic Programming

Import the following optimization packages:

```scala
import optimus.optimization._
import optimus.optimization.enums.SolverLib
import optimus.optimization.model.MPFloatVar
```

Create a model and select a solver for it:

```scala
implicit val model: MPModel = MPModel(SolverLib.oJSolver)
```

Ok! Let's create a couple of variables:

```scala
// Both variables are positive, that is, bounds are in [0, +inf]
val x = MPFloatVar.positive("x")
val y = MPFloatVar.positive("y")
```

Then we can define our optimization problem subject to a couple of constraints using our known maths:

```scala
minimize(-8*x - 16*y + x*x + 4*y^2)
subjectTo(
          x + y <:= 5,
          x <:= 3
         )
```

At last, we can solve the problem by starting the solver and displaying the results:

```scala
start()

println(s"objective: $objectiveValue")
println(s"x = ${x.value} y = ${y.value}")
```

Finally, don't forget to release the memory used by the internal solver:

```scala
release()
```
