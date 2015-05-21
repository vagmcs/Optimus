## Optimus

Optimus is an experimental library for Linear and Quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Licence 

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

#### Features:
1. High level mathematical modeling in Scala using algebraic expressions
  * Linear and quadratic objective and constraint expressions
  * Higher order expressions cannot be defined or handled by the solvers yet
  * Addition, subtraction and multiplication operations can be performed on expressions
  * Exponentiation can be performed on variables (up to quadratic)
2. Supports various optimization settings by using existing mathematical programming solvers
  * Linear programming (LP)
  * Quadratic programming (QP)
  * Quadratic constraint quadratic programming (QCQP)
  * Mixed integer programming (MIP)
3. Available solvers:
  * Open source [lp_solve](http://sourceforge.net/projects/lpsolve/) can be used for LP and MIP
  * Open source [ojalgo](http://ojalgo.org/) can be used for LP, QP and MIP
  * Proprietary solver [Gurobi](http://www.gurobi.com/) can be used for efficiency to solve LP, QP, QCQP and MIP

#### Future work:
1. Mathematical operations for division and non-linear functions (logarithmic, trigonometric etc.)
2. Mosek proprietary solver interface

## Instructions to build Optimus from source

In order to build Optimus from source, you need to have Java 8 and [sbt](http://www.scala-sbt.org/) installed in your system. Furthermore, Optimus build optionally depends on [Gurobi](http://www.gurobi.com/). In case the dependencies for Gurobi are not included, Optimus would build a minimal version having only lp_solve and ojalgo. 

Step 1. Optionally, include Gurobi library dependencies to `./lib`, as it is illustrated in the tree below:

```
lib/
|-- gurobi.jar
```

Step 2. For using Gurobi and lp_solve you should also set the environment variables of your system to make use of the solver native executable files.

Step 3. To build the Optimus distribution type the following command:

```
$ sbt dist
```

After a successful compilation, distribution is located inside the `./target/universal/optimus-<version>.zip` file. The distribution contains all library dependencies and requires only Java 8 (or higher). Sources, documentation and the compiled library (without dependencies) are archived as jar files into the `./target/scala-2.11/` directory.
	

## Local publish	
To publish to your local Apache Ivy directory (e.g., inside ~/.ivy2/local/), type the following command:

```
$ sbt publishLocal
```

## Example of LP:

Import the optimization package:

```scala
scala> import optimus.optimization._
```

Create a linear-quadratic problem and select a solver for it:

```scala
scala> implicit val problem = LQProblem(SolverLib.oJalgo)
```

Ok! Let's create a couple of variables:

```scala
scala> val x = MPFloatVar("x", 100, 200)
val y = MPFloatVar("y", 80, 170)
```

Then we can define our optimization problem subject to a simple constraint using our known maths:

```scala
scala> maximize(-2 * x + 5 * y)
add(y >= -x + 200)
```

At last, we can solve the problem by starting the solver and displaying the results:

```scala
scala> start()
println("objective: " + objectiveValue)
println("x = " + x.value + "y = " + y.value)
```

Finally, don't forget to release the memory used by the internal solver:

```scala
scala> release()
```

## Example of QP:

Import the optimization package:

```scala
scala> import optimus.optimization._
```

Create a linear-quadratic problem and select a solver for it:

```scala
scala> implicit val problem = LQProblem(SolverLib.oJalgo)
```

Ok! Let's create a couple of variables:
```scala
scala> val x = MPFloatVar("x", 0, Double.PositiveInfinity)
val y = MPFloatVar("y", 0, Double.PositiveInfinity)
```      

Then we can define our optimization problem subject to a couple of constraints using our known maths:

```scala
scala> minimize(-8*x - 16*y + x*x + 4*y^2)
subjectTo(
          x + y <= 5,
          x <= 3
         )
```

At last, we can solve the problem by starting the solver and displaying the results:

```scala
scala> start()
println("objective: " + objectiveValue)
println("x = " + x.value + "y = " + y.value)
```      

Finally, don't forget to release the memory used by the internal solver:

```scala
scala> release()
```

## Example of MIP:

Import the optimization package:

```scala
scala> import optimus.optimization._
```

Create a linear-quadratic problem and select a solver for it:

```scala
scala> implicit val problem = MIProblem(SolverLib.oJalgo)
```

Ok! Let's create a couple of float and a couple of integer variables:
```scala
scala> val x = MPFloatVar("x", 0, 40)
val y = MPIntVar("y", 0 to 1000)
val z = MPIntVar("z", 0 until 18)
val t = MPFloatVar("t", 2, 3)
``` 

Then we can define our optimization problem subject to some constraints using our known maths:

```scala
scala> maximize(x + 2*y + 3*z + t)
subjectTo(
          -1*x + y + z + 10*t <= 20,
          x - 3.0*y + z <= 30,
          y - 3.5*t := 0
         )
```

At last, we can solve the problem by starting the solver and displaying the results:

```scala
scala> start()
println("objective: " + objectiveValue)
println("x = " + x.value + "y = " + y.value + "z = " + z.value + "t = " + t.value)
```

Finally, don't forget to release the memory used by the internal solver:

```scala
scala> release()
```