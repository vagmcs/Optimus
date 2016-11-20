## Quadratic Programming

Import the optimization package:

```scala
import optimus.optimization._
```

Create a linear-quadratic problem and select a solver for it:

```scala
implicit val problem = LQProblem(SolverLib.ojalgo)
```

Ok! Let's create a couple of variables:
```scala
val x = MPFloatVar("x", 0, Double.PositiveInfinity)
val y = MPFloatVar("y", 0, Double.PositiveInfinity)
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
println("objective: " + objectiveValue)
println("x = " + x.value + "y = " + y.value)
```

Finally, don't forget to release the memory used by the internal solver:

```scala
release()
```