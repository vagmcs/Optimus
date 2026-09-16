package optimus.optimization

import optimus.optimization.enums.SolverLib

final class CbcProblemTests extends ProblemTests {
  def solver: SolverLib = SolverLib.Cbc
}
