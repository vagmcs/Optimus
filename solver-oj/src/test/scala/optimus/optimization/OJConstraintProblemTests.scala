package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra._
import optimus.optimization.SolverLib.SolverLib
import optimus.optimization._

/**
  * N-Queens puzzle using MIP programming.
  */
final class OJConstraintProblemTests extends ConstraintProblemTests {

  def solver: SolverLib = SolverLib.ojalgo
}
