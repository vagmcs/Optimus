package optimus.optimization

import scala.collection.mutable.ArrayBuffer
import com.typesafe.scalalogging.StrictLogging
import optimus.algebra.Expression
import optimus.optimization.enums.PreSolve.DISABLED
import optimus.optimization.enums.SolutionStatus.NOT_SOLVED
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

/**
 * Solver interface that describes a mathematical programming solver. It should
 * be extended by all the individual solvers to be supported in the library.
 */
trait MPSolver extends StrictLogging {

  protected var numberOfVars: Int = 0
  protected var numberOfCons: Int = 0
  protected var _objectiveValue: Option[Double] = None
  protected var _solution: Array[Double] = Array.emptyDoubleArray
  protected var _solutionStatus: SolutionStatus = NOT_SOLVED

  type Solver

  protected var underlyingSolver: Solver

  /**
   * Problem builder, should configure the solver and append
   * mathematical model variables and constraints.
   *
   * @param numberOfVars number of variables in the model
   */
  def buildModel(numberOfVars: Int): Unit

  /** @return the number of variables in the solver. */
  def numberOfVariables: Int = numberOfVars

  /** @return the number of constraints in the solver. */
  def numberOfConstraints: Int = numberOfCons

  /**
   * Get value of the variable in the specified position. Solution
   * should exist in order for a value to exist.
   *
   * @param colId position of the variable
   * @return the value of the variable in the solution
   */
  def getVarValue(colId: Int): Double

  /**
   * Set bounds of variable in the specified position.
   *
   * @param colId position of the variable
   * @param lower domain lower bound
   * @param upper domain upper bound
   */
  def setBounds(colId: Int, lower: Double, upper: Double): Unit

  /**
   * Set bot upper and lower bounds to unbounded (infinite).
   *
   * @param colId position of the variable
   */
  def setDoubleUnbounded(colId: Int): Unit = {
    setUnboundLowerBound(colId)
    setUnboundUpperBound(colId)
  }

  /**
   * Set upper bound to unbounded (infinite).
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int): Unit

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int): Unit

  /**
   * Set the column/variable as an integer variable
   *
   * @param colId position of the variable
   */
  def setInteger(colId: Int): Unit

  /**
   * Set the column / variable as an binary integer variable
   *
   * @param colId position of the variable
   */
  def setBinary(colId: Int): Unit

  /**
   * Set the column/variable as a float variable
   *
   * @param colId position of the variable
   */
  def setFloat(colId: Int): Unit

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def setObjective(objective: Expression, minimize: Boolean): Unit

  /**
   * Add a mathematical programming constraint to the solver.
   *
   * @param mpConstraint the mathematical programming constraint
   */
  def addConstraint(mpConstraint: MPConstraint): Unit

  /**
   * Add all given mathematical programming constraints to the solver.
   *
   * @param constraints an array buffer containing the constraints
   */
  def addAllConstraints(constraints: ArrayBuffer[MPConstraint]): Unit = {
    var idx = 0
    val len = constraints.length
    while (idx < len) {
      addConstraint(constraints(idx))
      idx += 1
    }
  }

  /**
   * Solves the optimization problem.
   *
   * @param preSolve pre-solving mode
   * @return status code indicating the nature of the solution
   */
  def solve(preSolve: PreSolve = DISABLED): SolutionStatus

  /**
   * @note The objective value may be the best feasible solution
   *       if optimality is not attained or proven.
   * @return the objective value of the solution found by the solver
   */
  def objectiveValue: Option[Double] = _objectiveValue

  /**
   * @note the solution may be the best feasible solution found
   *       so far if optimality is not attained or proven.
   * @return the solution found by the solver, for each variable
   */
  def solution: Array[Double] = _solution

  /** @return the status of the solution found by the solver */
  def solutionStatus: SolutionStatus = _solutionStatus

  /** Release the memory of the solver. */
  def release(): Unit

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int): Unit
}
