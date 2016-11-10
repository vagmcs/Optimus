package optimus.optimization

import optimus.optimization.SolverLib.SolverLib

/**
  * N-Queens puzzle using MIP programming.
  */
final class GurobiQueensTest extends QueensTest {

  def solver: SolverLib = SolverLib.gurobi
}
