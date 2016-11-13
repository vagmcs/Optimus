package optimus.optimization

import optimus.optimization.SolverLib.SolverLib

/**
  * N-Queens puzzle using MIP programming.
  */
final class LPQueensTest extends QueensTest {

  def solver: SolverLib = SolverLib.lp_solve
}
