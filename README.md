## Optimus

<a href="https://github.com/vagmcs/Optimus/actions/workflows/tester.yml" target="_blank">
    <img src="https://github.com/vagmcs/Optimus/actions/workflows/tester.yaml/badge.svg?event=push&branch=master" alt="Test">
</a>
<a href="https://results.pre-commit.ci/latest/github/vagmcs/Optimus/main" target="_blank">
    <img src="https://results.pre-commit.ci/badge/github/vagmcs/Optimus/master.svg" alt="pre-commit.ci status">
</a>
<a href="https://central.sonatype.com/artifact/com.github.vagmcs/optimus_3" target="_black">
    <img src="https://maven-badges.herokuapp.com/maven-central/com.github.vagmcs/optimus_2.13/badge.svg" alt="Maven Central">
</a>
<!-- <a href="https://scala-steward.org" target="_black">
    <img src="https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=" alt="Scala Steward">
</a> -->

Optimus is a library for Linear and Quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Features

1. High level mathematical modeling in Scala using algebraic expressions
  * Linear and quadratic objective and constraint expressions.
  * Higher order expressions cannot be defined or handled by the solvers yet.
  * Addition, subtraction and multiplication operations can be performed on expressions.
  * Expression simplification produces the simpler form of the expression.
2. Supports various optimization settings by using existing mathematical programming solvers
  * Linear programming (LP)
  * Quadratic programming (QP)
  * Quadratic constraint quadratic programming (QCQP)
  * Mixed integer programming (MIP)
3. Available solvers:
  * Open source [LpSolve](http://sourceforge.net/projects/lpsolve/) can be used for LP and MIP.
  * Open source [oJSolver](http://ojalgo.org/) can be used for LP, QP and MIP.
  * Proprietary solver [Gurobi 12](http://www.gurobi.com/) can be used for efficiently solving LP, QP, QCQP and MIP.
  * Proprietary solver [Mosek 9](https://www.mosek.com/) can be used for efficiently solving LP, QP, QCQP and MIP.

## How to get Optimus

Optimus is published to Maven Central for Scala 2.12, 2.13 and 3.3.4!

Add the following dependencies to your SBT build file in order to get started:

```scala
libraryDependencies ++= Seq(
    "com.github.vagmcs" %% "optimus" % "3.4.5",
    "com.github.vagmcs" %% "optimus-solver-oj" % "3.4.5",
    "com.github.vagmcs" %% "optimus-solver-lp" % "3.4.5"
)
```

Optionally, you can also add the following extra dependencies for proprietary solvers:

```scala
"com.github.vagmcs" %% "optimus-solver-gurobi" % "3.4.5"
"com.github.vagmcs" %% "optimus-solver-mosek" % "3.4.5"
```

For more information see [Building and Linking](docs/building_and_linking.md)

## Documentation

- [Linear Programming](docs/linear.md)
- [Quadratic Programming](docs/quadratic.md)
- [Mixed Integer Programming](docs/mixed_integer.md)
- [Model Specification](docs/model_spec.md)

## Contributions

Contributions are welcome, for details see [CONTRIBUTING.md](CONTRIBUTING.md).

## License

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

