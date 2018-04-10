package optimus.optimization

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra.Expression
import optimus.optimization.enums.{PreSolve, ProblemStatus}
import scala.collection.mutable.ArrayBuffer

/**
  * Solver interface that describes a mathematical programming solver. It should
  * be extended by all the individual solvers to be supported in the library.
  */
trait MPSolver extends StrictLogging {

  /**
    * Number of rows in the model
    */
  var nbRows: Int // TODO should not be in the interface

  /**
    * Number of columns / variables in the model
    */
  var nbCols: Int // TODO should not be in the interface

  /**
    * Solution, one entry for each column / variable
    */
  var solution: Array[Double] // TODO should be a function

  /**
    * Objective value
    */
  var objectiveValue: Double // TODO should be a function

  def solutionStatus: ProblemStatus = ???

  def objectiveBound: Double = ???

  /**
    * Problem builder, should configure the solver and append
    * mathematical model variables.
    *
    * @param nbRows rows in the model
    * @param nbCols number of variables in the model
    */
  def buildProblem(nbRows: Int, nbCols: Int)

  /**
    * Get value of the variable in the specified position. Solution
    * should exist in order for a value to exist.
    *
    * @param colId position of the variable
    * @return the value of the variable in the solution
    */
  def getValue(colId: Int): Double

  /**
    * Set bounds of variable in the specified position.
    *
    * @param colId position of the variable
    * @param lower domain lower bound
    * @param upper domain upper bound
    */
  def setBounds(colId: Int, lower: Double, upper: Double)

  /**
    * Set upper bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundUpperBound(colId: Int)

  /**
    * Set lower bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundLowerBound(colId: Int)

  /**
    * Set the column/variable as an integer variable
    *
    * @param colId position of the variable
    */
  def setInteger(colId: Int)

  /**
    * Set the column / variable as an binary integer variable
    *
    * @param colId position of the variable
    */
  def setBinary(colId: Int)

  /**
    * Set the column/variable as a float variable
    *
    * @param colId position of the variable
    */
  def setFloat(colId: Int)

  /**
    * Add objective expression to be optimized by the solver.
    *
    * @param objective the expression to be optimized
    * @param minimize flag for minimization instead of maximization
    */
  def setObjective(objective: Expression, minimize: Boolean)

  /**
    * Add a mathematical programming constraint to the solver.
    *
    * @param mpConstraint the mathematical programming constraint
    */
  def addConstraint(mpConstraint: MPConstraint)

  /**
    * Add all given mathematical programming constraints to the solver.
    *
    * @param constraints array buffer containing the constraints
    */
  def addAllConstraints(constraints: ArrayBuffer[MPConstraint]) {
    var idx = 0
    val len = constraints.length
    while(idx < len) {
      addConstraint(constraints(idx))
      idx += 1
    }
    logger.info("Added " + len + " constraints")
  }

  /**
    * Solve the problem.
    *
    * @param preSolve pre-solving mode
    * @return status code indicating the nature of the solution
    */
  def solveProblem(preSolve: PreSolve = PreSolve.DISABLED): ProblemStatus

  /**
    * Release the memory of this solver
    */
  def release(): Unit

  /**
    * Set a time limit for solver optimization. After the limit
    * is reached the solver stops running.
    *
    * @param limit the time limit
    */
  def setTimeout(limit: Int): Unit
}
