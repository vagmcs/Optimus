package optimus.optimization

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
import optimus.optimization.SolverLib._

import scala.util.{Success, Try}

/**
 * Models for mathematical programming. Everything related to this field of
 * optimization should be included here.
 *
 * @author Vagelis Michelioudakis
 */


/**
 * A Linear-Quadratic problem. Can be solved using one of the supported
 * solvers (LPSolve, oJalgo or Gurobi).
 *
 * @param solverLib solver library type
 */
class LQProblem private[optimization](solverLib: SolverLib = SolverLib.oJalgo) extends AbstractMPProblem {

  val solver = solverLib match {

    case SolverLib.gurobi =>
      Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("Gurobi is not supported in this build. Please rebuild Optimus with Gurobi dependencies.")
      }

    case SolverLib.lp_solve => new LPSolve

    case _ => new OJalgo
  }
}

object LQProblem {
  def apply(solverLib: SolverLib = SolverLib.oJalgo): LQProblem = new LQProblem(solverLib)
}

/**
 * A Mixed-Integer problem. Can be solved using one of the supported
 * solvers (LPSolve, oJalgo or Gurobi).
 *
 * @param solverLib solver library type
 */
class MIProblem private[optimization](solverLib: SolverLib = SolverLib.oJalgo) extends AbstractMPProblem {

  val solver = solverLib match {

    case SolverLib.gurobi =>
      Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("Gurobi is not supported in this build. Please rebuild Optimus with Gurobi dependencies.")
      }

    case SolverLib.lp_solve => new LPSolve

    case _ => new OJalgo
  }

  override protected def setVariableProperties() = {
    super.setVariableProperties()

    for (x <- variables) {
      if(x.isBinary) solver.setBinary(x.index)
      else if(x.isInteger) solver.setInteger(x.index)
    }
  }
}

object MIProblem {
  def apply(solverLib: SolverLib = SolverLib.oJalgo): MIProblem = new MIProblem(solverLib)
}

/**
 * Mathematical programming integer/binary variables.
 *
 * @param problem the linear-quadratic problem the variable belongs
 * @param domain the variable domain defined by a range of integers. If the range is [0,1]
 *               then the variable is
 * @param symbol the symbol of the variable (default is anonymous)
 */
final class MPIntVar private(problem: AbstractMPProblem, domain: Range, override val symbol: String = Variable.ANONYMOUS)
  extends MPVariable(problem, domain.min, domain.max, false, symbol) {
  
  this.integer = true
  this.binary = domain.min == 0 && domain.max == 1
}

object MPIntVar {

  def apply(symbol: String,  domain: Range)(implicit problem: AbstractMPProblem) =  new MPIntVar(problem, domain, symbol)
  
  def apply(domain: Range)(implicit problem: AbstractMPProblem) = new MPIntVar(problem, domain)
  
  def apply(problem: AbstractMPProblem, symbol: String,  domain: Range) = new MPIntVar(problem, domain, symbol)
}

/**
 * Mathematical programming float variables (bounded or unbounded).
 *
 * @param problem the linear-quadratic problem the variable belongs
 * @param lBound the lower bound of variable domain (default is 0.0)
 * @param uBound the upper bound of variable domain (default is infinite)
 * @param symbol the symbol of the variable (default is anonymous)
 */
final class MPFloatVar private(problem: AbstractMPProblem, val lBound: Double = 0.0, val uBound: Double = Double.PositiveInfinity,
                               override val symbol: String = Variable.ANONYMOUS) extends MPVariable(problem, lBound, uBound, false, symbol) {

  def this(problem: AbstractMPProblem, unbounded: Boolean) = {
    this(problem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity)
    this.unbounded = unbounded
  }

  def this(symbol: String, problem: AbstractMPProblem, unbounded: Boolean) = {
    this(problem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity, symbol)
    this.unbounded = unbounded
  }
}

object MPFloatVar {

  def apply()(implicit problem: AbstractMPProblem) = new MPFloatVar(problem)

  def apply(unbounded: Boolean)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, unbounded)

  def apply(implicit problem: AbstractMPProblem, unbounded: Boolean) = new MPFloatVar(problem, unbounded)

  def apply(lBound: Double, uBound: Double)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, lBound, uBound)

  def apply(implicit problem: AbstractMPProblem, lBound: Double, uBound: Double) = new MPFloatVar(problem, lBound, uBound)

  def apply(symbol: String)(implicit problem: AbstractMPProblem) = new MPFloatVar(symbol, problem, false)

  def apply(symbol: String, unbounded: Boolean)(implicit problem: AbstractMPProblem) = new MPFloatVar(symbol, problem, unbounded)

  def apply(implicit problem: AbstractMPProblem, symbol: String, unbounded: Boolean) = new MPFloatVar(symbol, problem, unbounded)

  def apply(symbol: String, lBound: Double, uBound: Double)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, lBound, uBound, symbol)

  def apply(implicit problem: AbstractMPProblem, symbol: String, lBound: Double, uBound: Double) = new MPFloatVar(problem, lBound, uBound, symbol)
}