## Linear Programming

Import the optimization package:

```scala
import optimus.optimization._
```

Create a linear-quadratic problem and select a solver for it:

```scala
implicit val problem = LQProblem(SolverLib.oJalgo)
```

Ok! Let's create a couple of variables:

```scala
val x = MPFloatVar("x", 100, 200)
val y = MPFloatVar("y", 80, 170)
```

Then we can define our optimization problem subject to a simple constraint using our known maths:

```scala
maximize(-2 * x + 5 * y)
add(y >= -x + 200)
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
