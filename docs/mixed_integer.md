## Mixed-Integer Programming

Import the following optimization packages:

```scala
import optimus.optimization._
import optimus.optimization.enums.SolverLib
import optimus.optimization.model.MPFloatVar
```

Create a model problem and select a solver for it:

```scala
implicit val model = MPModel(SolverLib.oJSolver)
```

Ok! Let's create a couple of float and a couple of integer variables:
```scala
val x = MPFloatVar("x", 0, 40)
val y = MPIntVar("y", 0 to 1000)
val z = MPIntVar("z", 0 until 18)
val t = MPFloatVar("t", 2, 3)
```

Then we can define our optimization problem subject to some constraints using our known maths:

```scala
maximize(x + 2*y + 3*z + t)
subjectTo(
          -1*x + y + z + 10*t <:= 20,
          x - 3.0*y + z <:= 30,
          y - 3.5*t := 0
         )
```

At last, we can solve the problem by starting the solver and displaying the results:

```scala
start()

println(s"objective: $objectiveValue")
println(s"x = ${x.value} y = ${y.value} z = ${z.value} t = ${t.value}")
```

Finally, don't forget to release the memory used by the internal solver:

```scala
release()
```
