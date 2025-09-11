package optimus.optimization

import optimus.optimization.enums.SolverLib

final class GurobiProblemTests extends ProblemTests {
  def solver: SolverLib = SolverLib.Gurobi
}
