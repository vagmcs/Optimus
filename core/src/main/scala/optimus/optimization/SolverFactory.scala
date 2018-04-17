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

import com.typesafe.scalalogging.StrictLogging
import optimus.optimization.enums.SolverLib
import optimus.optimization.enums.SolverLib._

import scala.util.{Success, Try}

object SolverFactory extends StrictLogging {

  lazy val solvers: List[SolverLib] =
    List(LpSolve, oJSolver, Gurobi, Mosek).filter(canInstantiateSolver(_).isSuccess)

  lazy val quadraticSolvers: List[SolverLib] =
    List(oJSolver, Gurobi, Mosek).filter(canInstantiateSolver(_).isSuccess)

  // Checks if the given solver can be ran on this system
  private[optimization] def canInstantiateSolver(s: SolverLib): Try[MPSolver] = Try {
    SolverFactory.instantiate(s)
  }

  def instantiate(solver: SolverLib): MPSolver = solver match {
    case Gurobi =>
      Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.newInstance.asInstanceOf[MPSolver]
        case _ => sys.error("Gurobi dependency is missing.")
      }

    case Mosek =>
      Try(Class.forName("optimus.optimization.Mosek")) match {
        case Success(c) => c.newInstance.asInstanceOf[MPSolver]
        case _ => sys.error("Mosek dependency is missing.")
      }

    case LpSolve =>
      Try(Class.forName("optimus.optimization.LPSolve")) match {
        case Success(c) => c.newInstance.asInstanceOf[MPSolver]
        case _ => sys.error("LPSolve dependency is missing.")
      }

    case _ =>
      Try(Class.forName("optimus.optimization.oJSolver")) match {
        case Success(c) => c.newInstance.asInstanceOf[MPSolver]
        case _ => sys.error("ojSolver dependency is missing.")
      }
  }
}
