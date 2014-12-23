## Optimus

Optimus is an experimental library for linear and quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Licence 

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

#### Features overview:
1. High level mathematical modeling in Scala using algebraic expressions
  * Linear and quadratic objective and constraint expressions
  * Expressions currently support addition, subtraction and multiplication operations
2. Supports linear programming (LP), quadratic programming (QP) and quadratic constraint quadratic programming (QCQP) by using existing mathematical programming solvers.
3. Solvers currently available:
  * Open-source [LPsolve](http://sourceforge.net/projects/lpsolve/) can be used for LP
  * Open-source [oJalgo](http://ojalgo.org/) can be used for LP and QP
  * Commercial solver [Gurobi](http://www.gurobi.com/) can be used for more efficiency to solve LP, QP and QCQP

#### Future work
1. Support division operation and non-linear functions (logarithm, sine, cosine, etc)
2. Mosek commercial solver interface

## Instrunctions to build Optimus from source

In order to build Optimus from source, you need to have Java 7 and [sbt](http://www.scala-sbt.org/) installed in your system.
