package optimus.optimization

import optimus.optimization.enums.SolverLib

final class GlopProblemTests extends LPProblemTests {
  def solver: SolverLib = SolverLib.Glop
}
