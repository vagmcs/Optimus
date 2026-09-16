package optimus.optimization.enums

sealed trait SolverLib

object SolverLib {

  case object oJSolver extends SolverLib
  case object LpSolve extends SolverLib
  case object Cbc extends SolverLib
  case object Scip extends SolverLib
  case object Glop extends SolverLib
  case object Sat extends SolverLib
  case object Gurobi extends SolverLib
  case object Mosek extends SolverLib
}
