package optimus.optimization

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra._
import optimus.common.Measure._
import optimus.optimization.enums.PreSolve.DISABLED
import optimus.optimization.enums.{ PreSolve, SolutionStatus, SolverLib }
import optimus.optimization.model.{ MPConstraint, MPVar }
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/**
 * Defines the mathematical programming model we are about to solve.
 *
 * @param solverLib a solver library (default is ojSolver)
 */
case class MPModel(solverLib: SolverLib = SolverLib.oJSolver) extends StrictLogging {

  protected val variables: ArrayBuffer[MPVar] = ArrayBuffer.empty[MPVar]
  protected val constraints: ArrayBuffer[MPConstraint] = ArrayBuffer.empty[MPConstraint]
  protected var solution = mutable.HashMap.empty[Int, Double]
  protected var objective: Expression = 0
  protected var minimize = false
  protected var reOptimize = false

  protected lazy val solver: MPSolver = SolverFactory.instantiate(solverLib)

  protected def optimize(expression: Expression, minimize: Boolean): MPModel = {
    reOptimize = false
    objective = expression
    this.minimize = minimize
    this
  }

  /**
   * Register a variable to the model
   *
   * @see [[optimus.optimization.model.MPVar]]
   *
   * @param variable an MPVar to register
   * @return the index of the variable
   */
  def register(variable: MPVar): Int = {
    variables += variable
    variables.length - 1
  }

  /**
   * @see [[optimus.optimization.model.MPVar]]
   *
   * @param idx the index of the variable
   * @return the MPVar on the given index
   */
  def variable(idx: Int): Try[MPVar] = Try {
    variables(idx)
  }

  /**
   * @param idx the index of the variable
   * @return the solution value for the variable
   */
  def getVarValue(idx: Int): Option[Double] = solution.get(idx)

  /**
   * @see [[optimus.algebra.Constraint]]
   *
   * @param constraint a constraint to add
   * @return an MPConstraint
   */
  def add(constraint: Constraint): MPConstraint = {
    val constraintToAdd = MPConstraint(constraint, constraints.size, this)
    constraints += constraintToAdd
    if (reOptimize) solver.addConstraint(constraintToAdd)
    constraintToAdd
  }

  /** @return the objective value of the underlying solver */
  def objectiveValue: Double = solver.objectiveValue.get

  /**
   * @param expression an expression to minimize
   * @return the model
   */
  def minimize(expression: Expression): MPModel = optimize(expression, minimize = true)

  /**
   * @param expression an expression to maximize
   * @return the model
   */
  def maximize(expression: Expression): MPModel = optimize(expression, minimize = false)

  /**
   * Start the underlying solver.
   *
   * @see [[optimus.optimization.enums.PreSolve]]
   *
   * @param timeLimit a time limit for the solver
   * @param preSolve a pre solve strategy
   * @return true if there is a solution, false otherwise
   */
  def start(timeLimit: Int = Int.MaxValue, preSolve: PreSolve = DISABLED): Boolean = {

    if (!reOptimize) {
      solver.buildModel(variables.size)

      measureTime(s"Variables (${variables.length}) configured in:") {
        for (x <- variables) {
          if (x.isUnbounded) solver.setDoubleUnbounded(x.index)
          else solver.setBounds(x.index, x.lowerBound, x.upperBound)

          if (x.isBinary) solver.setBinary(x.index)
          else if (x.isInteger) solver.setInteger(x.index)
        }
      }

      measureTime("Objective function added in:") {
        solver.setObjective(objective, minimize)
      }

      measureTime(s"Constraints (${constraints.length}) created in:") {
        solver.addAllConstraints(constraints)
      }

      reOptimize = true
    } else logger.info("Re-optimizing...")

    if (timeLimit < Int.MaxValue) solver.setTimeout(timeLimit)

    val status = measureTime("Solution found in:") {
      solver.solve(preSolve)
    }

    if ((status == SolutionStatus.OPTIMAL) || (status == SolutionStatus.SUBOPTIMAL)) variables.indices foreach { i =>
      solution(i) = solver.getVarValue(i)
    }

    logger.info(s"Solution status is $status.")
    (status == SolutionStatus.OPTIMAL) || (status == SolutionStatus.SUBOPTIMAL)
  }

  /**
   * Check if all constraints in the model are satisfied by
   * the given solution.
   *
   * @param tol a tolerance threshold
   * @return true if all constraints are satisfied, false otherwise
   */
  def checkConstraints(tol: Double = 10e-6): Boolean = constraints.forall(_.check(tol))

  /** @return the status of the solution found for the model */
  def getStatus: SolutionStatus = solver.solutionStatus

  /** Release the memory of the underlying solver. */
  def release(): Unit = solver.release()
}
