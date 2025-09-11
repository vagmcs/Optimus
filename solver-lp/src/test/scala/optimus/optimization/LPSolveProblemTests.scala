package optimus.optimization

import optimus.optimization.enums.SolverLib

final class LPSolveProblemTests extends LPProblemTests {
  def solver: SolverLib = SolverLib.LpSolve
}
