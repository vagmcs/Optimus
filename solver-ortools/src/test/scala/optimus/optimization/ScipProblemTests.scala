package optimus.optimization

import optimus.optimization.enums.SolverLib

final class ScipProblemTests extends ProblemTests {
  def solver: SolverLib = SolverLib.Scip
}
