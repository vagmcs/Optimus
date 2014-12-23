## Optimus

Optimus is an experimental library for Linear and Quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Licence 

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

#### Features overview:
1. High level mathematical modeling in Scala using algebraic expressions
  * Linear and quadratic objective and constraint expressions
  * Algebra also supports and higher order expressions, but they cannot be solved by the existing solvers
  * Expressions currently support addition, subtraction and multiplication operations
2. Support for linear programming (LP), quadratic programming (QP) and quadratic constraint quadratic programming (QCQP) by using existing mathematical programming solvers.
3. Available solvers:
  * Open source [LPsolve](http://sourceforge.net/projects/lpsolve/) can be used for LP
  * Open source [ojAlgo](http://ojalgo.org/) can be used for LP and QP
  * Commercial solver [Gurobi](http://www.gurobi.com/) can be used for more efficiency to solve LP, QP and QCQP

#### Future work:
1. Mathematical operations for division and non-linear functions (logarithmic, trigonometric etc.)
2. Mixed integer mathematical programming
3. Mosek commercial solver interface

## Instructions to build Optimus from source

In order to build Optimus from source, you need to have Java 7 and [sbt](http://www.scala-sbt.org/) installed in your system.

##### To start building the Optimus distribution, type the following command:

```
$ sbt dist
```

After a successful compilation, the distribution is located inside the `./target/universal/optimus-<version>.zip` file. The distribution contains all library dependencies and requires only a Java 7 (or higher runtime). Sources, documentation and the compiled library (without dependencies) are archived as jar files into the `./target/scala-2.10/` directory.