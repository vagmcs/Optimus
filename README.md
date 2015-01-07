## Optimus

Optimus is an experimental library for Linear and Quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Licence 

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

#### Features overview:
1. High level mathematical modeling in Scala using algebraic expressions
  * Linear and quadratic objective and constraint expressions
  * Higher order expressions are also supported, but they cannot be handled by the solvers
  * Addition, subtraction and multiplication operations can be performed on expressions
2. Support for linear programming (LP), quadratic programming (QP) and quadratic constraint quadratic programming (QCQP) by using existing mathematical programming solvers.
3. Available solvers:
  * Open source [lp_solve](http://sourceforge.net/projects/lpsolve/) can be used for LP
  * Open source [ojAlgo](http://ojalgo.org/) can be used for LP and QP
  * Commercial solver [Gurobi](http://www.gurobi.com/) can be used for more efficiency to solve LP, QP and QCQP

#### Future work:
1. Mathematical operations for division and non-linear functions (logarithmic, trigonometric etc.)
2. Mixed integer mathematical programming
3. Mosek commercial solver interface

## Instructions to build Optimus from source

In order to build Optimus from source, you need to have Java 7 and [sbt](http://www.scala-sbt.org/) installed in your system. Furthermore, Optimus build depends on the [lp_solve](http://lpsolve.sourceforge.net), [Gurobi](http://www.gurobi.com/) and [ojAlgo](http://ojalgo.org/).

Step 1. Include lp_solve, Gurobi and ojAlgo library dependencies to `./lib`, as it is illustrated in the tree below:

```
.
|-- gurobi.jar
|-- lpsolve55j.jar
|-- ojalgo-37.0.jar
|-- ojalgo-biz-37.0.jar
|-- ojalgo-ext-37.0.jar

```

Step 2. For using Gurobi and lp_solve you should also set the environment variables of your system to make use of the solver native executable files.

Step 3. To build the Optimus distribution type the following command:

```
$ sbt dist
```

After a successful compilation, distribution is located inside the `./target/universal/optimus-<version>.zip` file. The distribution contains all library dependencies and requires only Java 7 (or higher). Sources, documentation and the compiled library (without dependencies) are archived as jar files into the `./target/scala-2.10/` directory.

## Example:

Import the lqprog package:

```scala
scala> import optimus.lqprog._
```

Create a linear-quadratic problem and select a solver for it:

```scala
scala> implicit val problem = new LQProblem(SolverLib.OJalgo)
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

At last, we can solve the problem by starting the solver and display the results:

```scala
scala> start()
println("objective: " + objectiveValue)
println("x = " + x.value + "y = " + y.value)
```

Finally, don't forget to release the memory used by the internal solver:

```scala
scala> release()
```
