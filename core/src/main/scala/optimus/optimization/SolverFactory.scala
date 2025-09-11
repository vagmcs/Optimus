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
 * The mathematical programming library for Scala.
 *
 */

package optimus.optimization

import com.typesafe.scalalogging.StrictLogging
import optimus.optimization.enums.SolverLib
import optimus.optimization.enums.SolverLib._
import scala.util.{ Success, Try }

object SolverFactory extends StrictLogging {

  lazy val solvers: List[SolverLib] = List(LpSolve, oJSolver, Gurobi, Mosek).filter(canInstantiateSolver(_).isSuccess)

  lazy val quadraticSolvers: List[SolverLib] = List(oJSolver, Gurobi, Mosek).filter(canInstantiateSolver(_).isSuccess)

  // Checks if the given solver can be ran on this system
  private[optimization] def canInstantiateSolver(s: SolverLib): Try[MPSolver] = Try {
    SolverFactory.instantiate(s)
  }

  def instantiate(solver: SolverLib): MPSolver = solver match {
    case Gurobi => Try(Class.forName("optimus.optimization.Gurobi")) match {
        case Success(c) => c.getDeclaredConstructor().newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("Gurobi dependency is missing.")
      }

    case Mosek => Try(Class.forName("optimus.optimization.Mosek")) match {
        case Success(c) => c.getDeclaredConstructor().newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("Mosek dependency is missing.")
      }

    case LpSolve => Try(Class.forName("optimus.optimization.LPSolve")) match {
        case Success(c) => c.getDeclaredConstructor().newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("LPSolve dependency is missing.")
      }

    case _ => Try(Class.forName("optimus.optimization.oJSolver")) match {
        case Success(c) => c.getDeclaredConstructor().newInstance().asInstanceOf[MPSolver]
        case _ => sys.error("ojSolver dependency is missing.")
      }
  }
}
