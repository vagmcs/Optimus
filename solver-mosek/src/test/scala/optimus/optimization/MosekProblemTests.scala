package optimus.optimization

import optimus.optimization.enums.SolverLib

final class MosekProblemTests extends ProblemTests {
  def solver: SolverLib = SolverLib.Mosek
}
