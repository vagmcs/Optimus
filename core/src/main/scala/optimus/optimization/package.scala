/*
 *
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
 *  Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 *  This file is part of Optimus.
 *
 *  Optimus is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  Optimus is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 *  License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra.{Constraint, Expression}
import optimus.optimization.enums.PreSolve

/**
  * Helper functions for linear-quadratic optimization
  */
package object optimization extends StrictLogging{

  object SolverLib extends Enumeration {

    type SolverLib = Value

    val lp_solve = Value("lp_solve")
    val gurobi = Value("gurobi")
    val ojalgo = Value("ojalgo")
    val mosek = Value("mosek")
  }

  // Used for testing multiple solvers at once
  lazy val solvers = List(SolverLib.lp_solve, SolverLib.gurobi, SolverLib.ojalgo, SolverLib.mosek).filter(canInstantiateSolver)

  // Checks if the given solver can be ran on this system
  private def canInstantiateSolver(s: SolverLib.Value): Boolean = {
    try SolverFactory.instantiate(s)
    catch {
      case e: Exception => logger.error(e.getMessage); return false
    }
    true
  }
  
  // Helper functions to model using an implicit mathematical programming problem

  def add(constraint: Constraint)(implicit problem: AbstractMPProblem) = problem.add(constraint)

  def subjectTo(constraints: Constraint*)(implicit problem: AbstractMPProblem) = constraints.foreach(add)

  def start(preSolve: PreSolve = PreSolve.DISABLED,
            timeLimit: Int = Int.MaxValue)(implicit problem: AbstractMPProblem) = problem.start(timeLimit, preSolve)

  def minimize(expression: Expression)(implicit problem: AbstractMPProblem) = problem.minimize(expression)

  def maximize(expression: Expression)(implicit problem: AbstractMPProblem) = problem.maximize(expression)

  def release()(implicit problem: AbstractMPProblem) = problem.release()

  def objectiveValue(implicit problem: AbstractMPProblem) = problem.objectiveValue()

  def status(implicit problem: AbstractMPProblem) = problem.getStatus

  def checkConstraints(tol: Double = 10e-6)(implicit problem: AbstractMPProblem) = problem.checkConstraints(tol)
}
