package optimus.lqprog

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

import optimus.algebra.Variable
import optimus.lqprog.SolverLib.SolverLib

/**
 * Model for linear-quadratic mathematical programming. Everything
 * related to this field of optimization should be included here.
 *
 * @author Vagelis Michelioudakis
 */


/**
 * A Linear-Quadratic problem. Can be solved using one of the supported
 * solvers (LPSolve, oJalgo, Gurobi or MOSEK).
 *
 * @param solverLib solver library type
 */
class LQProblem(solverLib: SolverLib = SolverLib.OJalgo) extends AbstractMPProblem {

  val solver = solverLib match {
    case SolverLib.gurobi => new Gurobi
    case SolverLib.mosek => new Mosek
    case SolverLib.lp_solve => new LPSolve
    case _ => new OJalgo
  }
}

/**
 * Mathematical programming float variables (bounded or unbounded).
 *
 * @param lqProblem the linear-quadratic problem the variable belongs
 * @param lBound the lower bound of variable domain (default is 0.0)
 * @param uBound the upper bound of variable domain (default is infinite)
 * @param symbol the symbol of the variable (default is anonymous)
 */
final class MPFloatVar private(val lqProblem: LQProblem, val lBound: Double = 0.0, val uBound: Double = Double.PositiveInfinity,
                               override val symbol: String = Variable.ANONYMOUS) extends MPVariable(lqProblem, lBound, uBound, false, symbol) {

  def this(lqProblem: LQProblem, unbounded: Boolean) = {
    this(lqProblem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity)
    this.unbounded = unbounded
  }

  def this(symbol: String, lqProblem: LQProblem, unbounded: Boolean) = {
    this(lqProblem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity, symbol)
    this.unbounded = unbounded
  }
}

object MPFloatVar {

  def apply()(implicit lqProblem: LQProblem) = new MPFloatVar(lqProblem)

  def apply(unbounded: Boolean)(implicit lqProblem: LQProblem) = new MPFloatVar(lqProblem, unbounded)

  def apply(implicit lqProblem: LQProblem, unbounded: Boolean) = new MPFloatVar(lqProblem, unbounded)

  def apply(lBound: Double, uBound: Double)(implicit lqProblem: LQProblem) = new MPFloatVar(lqProblem, lBound, uBound)

  def apply(implicit lqProblem: LQProblem, lBound: Double, uBound: Double) = new MPFloatVar(lqProblem, lBound, uBound)

  def apply(symbol: String)(implicit lqProblem: LQProblem) = new MPFloatVar(symbol, lqProblem, false)

  def apply(symbol: String, unbounded: Boolean)(implicit lqProblem: LQProblem) = new MPFloatVar(symbol, lqProblem, unbounded)

  def apply(implicit lqProblem: LQProblem, symbol: String, unbounded: Boolean) = new MPFloatVar(symbol, lqProblem, unbounded)

  def apply(symbol: String, lBound: Double, uBound: Double)(implicit lqProblem: LQProblem) = new MPFloatVar(lqProblem, lBound, uBound, symbol)

  def apply(implicit lqProblem: LQProblem, symbol: String, lBound: Double, uBound: Double) = new MPFloatVar(lqProblem, lBound, uBound, symbol)
}