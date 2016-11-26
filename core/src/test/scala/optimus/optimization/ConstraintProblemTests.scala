package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra._
import scala.util.Random
import optimus.optimization.SolverLib.SolverLib

/**
  * Different test problems which should be implemented by any solver to test their
  * proper functioning.
  */
trait ConstraintProblemTests extends KnapsackTest with QueensTest {

  /**
  	* Solver implementation to use for the test execution
  	*/
  def solver: SolverLib
}
