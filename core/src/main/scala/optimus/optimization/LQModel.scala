/*
 *
 *   /\\\\\
 *  /\\\///\\\
 * /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 * /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 * \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *  \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *    \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *       \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *          \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus.optimization

import optimus.optimization.SolverLib._
import scala.util.{Success, Try}

object SolverFactory {
  def instantiate(solverLib: SolverLib) = solverLib match {
    case SolverLib.gurobi =>
      Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("Gurobi dependency is missing.")
      }

    case SolverLib.mosek =>
      Try(Class.forName("optimus.optimization.Mosek")) match {
        case Success(c) => c.newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("Mosek dependency is missing.")
      }

    case SolverLib.lp_solve => 
      Try(Class.forName("optimus.optimization.LPSolve")) match {
        case Success(c) => c.newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("LPSolve dependency is missing.")
      }

    case _ => 
      Try(Class.forName("optimus.optimization.OJalgo")) match {
        case Success(c) => c.newInstance().asInstanceOf[MPSolver]
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
class LQProblem private[optimization](solverLib: SolverLib = SolverLib.ojalgo) extends MPModel {

  override protected def instantiateSolver(): MPSolver = SolverFactory.instantiate(solverLib)
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
class MIProblem private[optimization](solverLib: SolverLib = SolverLib.ojalgo) extends MPModel {

  override protected def instantiateSolver(): MPSolver = SolverFactory.instantiate(solverLib)

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