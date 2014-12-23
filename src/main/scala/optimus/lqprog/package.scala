package optimus

/*
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

import optimus.algebra.{Expression, Constraint}

/**
 * Helper functions for linear-quadratic optimization
 *
 * @author Vagelis Michelioudakis
 */
package object lqprog {

  object SolverLib extends Enumeration {

    type SolverLib = Value

    val lp_solve = Value("lp_solve")
    val gurobi = Value("gurobi")
    val mosek = Value("mosek")
    val OJalgo = Value("oJalgo")
  }

  // Helper functions to model using an implicit mathematical programming problem

  def add(constraint: Constraint)(implicit lqProblem: LQProblem) = lqProblem.add(constraint)

  def addAll(constraints: Constraint*)(implicit lqProblem: LQProblem) {
    constraints.foreach(add)
  }

  def start()(implicit lqProblem: LQProblem) = lqProblem.start()

  def minimize(expression: Expression)(implicit lqProblem: LQProblem) = lqProblem.minimize(expression)

  def maximize(expression: Expression)(implicit lqProblem: LQProblem) = lqProblem.maximize(expression)

  def release()(implicit lqProblem: LQProblem) = lqProblem.release()

  def objectiveValue(implicit lqProblem: LQProblem) = lqProblem.objectiveValue()

  def status(implicit lqProblem: LQProblem) = lqProblem.getStatus

  def checkConstraints(tol: Double = 10e-6)(implicit lqProblem: LQProblem) = lqProblem.checkConstraints(tol)
}
