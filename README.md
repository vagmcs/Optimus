## Optimus

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.vagmcs/optimus_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.vagmcs/optimus_2.11)
[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

Optimus is a library for Linear and Quadratic mathematical optimization written in [Scala programming language](http://scala-lang.org).

## Licence 

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions; See the [GNU Lesser General Public License v3 for more details](http://www.gnu.org/licenses/lgpl-3.0.en.html).

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
  * Proprietary solver [Gurobi](http://www.gurobi.com/) can be used for efficiently solving LP, QP, QCQP and MIP.
  * Proprietary solver [Mosek](https://www.mosek.com/) can be used for efficiently solving LP, QP, QCQP and MIP.

## Building

See [Building and Linking](docs/building_and_linking.md)

## Documentation
- [Linear Programming](docs/linear.md)
- [Quadratic Programming](docs/quadratic.md)
- [Mixed Integer Programming](docs/mixed_integer.md)
- [Model Specification](docs/model_spec.md)

## Contributions

Contributions are welcome, for details see [CONTRIBUTING.md](CONTRIBUTING.md).

## Reference in Scientific Publications
Please use the following BibTeX entry when you cite Optimus in your papers:

```
@misc{Optimus,
      author = {Evangelos Michelioudakis and Anastasios Skarlatidis},
      title = {Optimus: an open-source mathematical optimization library},
      url = {https://github.com/vagmcs/Optimus}
}
```