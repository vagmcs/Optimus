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

import optimus.algebra._
import optimus.optimization.SolverLib._

import scala.util.{Success, Try}

object SolverFactory {
  def instantiate(solverLib: SolverLib) = solverLib match {
    case SolverLib.gurobi =>
      Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("Gurobi dependency is missing.")
      }

    case SolverLib.mosek =>
      Try(Class.forName("optimus.optimization.Mosek")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("Mosek dependency is missing.")
      }

    case SolverLib.lp_solve => 
      Try(Class.forName("optimus.optimization.LPSolve")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("LPSolve dependency is missing.")
      }

    case _ => 
      Try(Class.forName("optimus.optimization.OJalgo")) match {
        case Success(c) => c.newInstance().asInstanceOf[AbstractMPSolver]
        case _ => sys.error("OJalgo dependency is missing.")
      }
  }
}

/**
  * A Linear-Quadratic problem. Can be solved using one of the supported
  * solvers (LPSolve, ojalgo, Gurobi or Mosek).
  *
  * @param solverLib solver library type
  */
class LQProblem private[optimization](solverLib: SolverLib = SolverLib.ojalgo) extends AbstractMPProblem {

  override protected def instantiateSolver(): AbstractMPSolver = SolverFactory.instantiate(solverLib)
}

object LQProblem {
  def apply(solverLib: SolverLib = SolverLib.ojalgo): LQProblem = new LQProblem(solverLib)
}

/**
  * A Mixed-Integer problem. Can be solved using one of the supported
  * solvers (LPSolve, ojalgo, Gurobi or Mosek).
  *
  * @param solverLib solver library type
  */
class MIProblem private[optimization](solverLib: SolverLib = SolverLib.ojalgo) extends AbstractMPProblem {

  override protected def instantiateSolver(): AbstractMPSolver = SolverFactory.instantiate(solverLib)

  override protected def setVariableProperties() = {
    super.setVariableProperties()

    for (x <- variables) {
      if(x.isBinary) solver.setBinary(x.index)
      else if(x.isInteger) solver.setInteger(x.index)
    }
  }
}

object MIProblem {
  def apply(solverLib: SolverLib = SolverLib.ojalgo): MIProblem = new MIProblem(solverLib)
}

/**
  * Mathematical programming integer/binary variables.
  *
  * @param problem the linear-quadratic problem the variable belongs
  * @param domain the variable domain defined by a range of integers. If the range is {0,1}
  *               then the variable is binary.
  * @param symbol the symbol of the variable (default is anonymous)
  */
final class MPIntVar private(problem: AbstractMPProblem, domain: Range, override val symbol: String = ANONYMOUS)
  extends MPVariable(problem, domain.min, domain.max, false, symbol) {
  
  this.integer = true
  this.binary = domain.min == 0 && domain.max == 1
}

object MPIntVar {

  def apply(domain: Range)(implicit problem: AbstractMPProblem) = new MPIntVar(problem, domain)

  def apply(problem: AbstractMPProblem, domain: Range) = new MPIntVar(problem, domain)

  def apply(symbol: String,  domain: Range)(implicit problem: AbstractMPProblem) =  new MPIntVar(problem, domain, symbol)
  
  def apply(problem: AbstractMPProblem, symbol: String,  domain: Range) = new MPIntVar(problem, domain, symbol)
}

/**
  * Mathematical programming float variables (bounded or double unbounded).
  *
  * @param problem the linear-quadratic problem the variable belongs
  * @param lBound the lower bound of variable domain (default is 0.0)
  * @param uBound the upper bound of variable domain (default is infinite)
  * @param symbol the symbol of the variable (default is anonymous)
  */
final class MPFloatVar private(problem: AbstractMPProblem, val lBound: Double = 0.0, val uBound: Double = Double.PositiveInfinity,
                               override val symbol: String = ANONYMOUS) extends MPVariable(problem, lBound, uBound, false, symbol) {

  def this(problem: AbstractMPProblem, unbounded: Boolean) = {
    this(problem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity)
    this.unbounded = unbounded
  }

  def this(problem: AbstractMPProblem, symbol: String, unbounded: Boolean) = {
    this(problem, if (unbounded) Double.PositiveInfinity else 0.0, Double.PositiveInfinity, symbol)
    this.unbounded = unbounded
  }
}

object MPFloatVar {

  def apply()(implicit problem: AbstractMPProblem) = new MPFloatVar(problem)

  def apply(unbounded: Boolean)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, unbounded)

  def apply(problem: AbstractMPProblem, unbounded: Boolean) = new MPFloatVar(problem, unbounded)

  def apply(lBound: Double, uBound: Double)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, lBound, uBound)

  def apply(problem: AbstractMPProblem, lBound: Double, uBound: Double) = new MPFloatVar(problem, lBound, uBound)

  def apply(symbol: String)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, symbol, false)

  def apply(problem: AbstractMPProblem, symbol: String) = new MPFloatVar(problem, symbol, false)

  def apply(symbol: String, unbounded: Boolean)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, symbol, unbounded)

  def apply(problem: AbstractMPProblem, symbol: String, unbounded: Boolean) = new MPFloatVar(problem, symbol, unbounded)

  def apply(symbol: String, lBound: Double, uBound: Double)(implicit problem: AbstractMPProblem) = new MPFloatVar(problem, lBound, uBound, symbol)

  def apply(problem: AbstractMPProblem, symbol: String, lBound: Double, uBound: Double) = new MPFloatVar(problem, lBound, uBound, symbol)
}