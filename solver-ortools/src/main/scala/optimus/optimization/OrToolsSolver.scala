package optimus.optimization

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver.ResultStatus
import com.google.ortools.linearsolver.{ MPSolver => ORSolver }
import optimus.algebra.ExpressionType.{ GENERIC, QUADRATIC }
import optimus.algebra._
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

/**
 * Base implementation for solvers accessed through the Google OR-Tools linear solver
 * wrapper (see [[https://developers.google.com/optimization/lp/mpsolver]]). Concrete
 * subclasses only need to select the underlying solver backend.
 *
 * @param solverId the OR-Tools identifier of the underlying solver backend
 */
abstract class OrToolsSolver(solverId: String) extends MPSolver {

  type Solver = ORSolver

  Loader.loadNativeLibraries()

  protected var underlyingSolver: Solver = ORSolver.createSolver(solverId)

  protected def solverObjective = underlyingSolver.objective()
  protected def variable(colId: Int) = underlyingSolver.variable(colId)

  /**
   * Problem builder, should configure the solver and append
   * mathematical model variables and constraints.
   *
   * @param numberOfVars number of variables in the model
   */
  def buildModel(numberOfVars: Int): Unit = {

    logger.info(s"Building model using OR-Tools '$solverId' solver.")

    underlyingSolver.suppressOutput()

    this.numberOfVars = numberOfVars
    for (i <- 0 until numberOfVars) underlyingSolver.makeVar(-ORSolver.infinity(), ORSolver.infinity(), false, s"x$i")
  }

  /**
   * Get value of the variable in the specified position. Solution
   * should exist in order for a value to exist.
   *
   * @param colId position of the variable
   * @return the value of the variable in the solution
   */
  def getVarValue(colId: Int): Double = variable(colId).solutionValue()

  /**
   * Set bounds of variable in the specified position.
   *
   * @param colId position of the variable
   * @param lower domain lower bound
   * @param upper domain upper bound
   */
  def setBounds(colId: Int, lower: Double, upper: Double): Unit = variable(colId).setBounds(lower, upper)

  /**
   * Set upper bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int): Unit = variable(colId).setUb(ORSolver.infinity())

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int): Unit = variable(colId).setLb(-ORSolver.infinity())

  /**
   * Set the column/variable as an integer variable
   *
   * @param colId position of the variable
   */
  def setInteger(colId: Int): Unit = variable(colId).setInteger(true)

  /**
   * Set the column / variable as an binary integer variable
   *
   * @param colId position of the variable
   */
  def setBinary(colId: Int): Unit = {
    variable(colId).setBounds(0, 1)
    variable(colId).setInteger(true)
  }

  /**
   * Set the column/variable as a float variable
   *
   * @param colId position of the variable
   */
  def setFloat(colId: Int): Unit = variable(colId).setInteger(false)

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def setObjective(objective: Expression, minimize: Boolean): Unit = {

    if (objective.getOrder == QUADRATIC || objective.getOrder == GENERIC) throw new IllegalArgumentException(
      s"$solverId (via OR-Tools) can handle only linear expressions and $objective is of higher order!"
    )

    if (minimize) solverObjective.setMinimization() else solverObjective.setMaximization()
    solverObjective.setOffset(objective.constant)

    val iterator = objective.terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      val indexes = decode(iterator.key)
      if (indexes.length == 1) solverObjective.setCoefficient(variable(indexes.head), iterator.value())
      else throw new IllegalArgumentException("Invalid expression " + objective)
    }
  }

  /**
   * Add a mathematical programming constraint to the solver.
   *
   * @param mpConstraint the mathematical programming constraint
   */
  def addConstraint(mpConstraint: MPConstraint): Unit = {
    numberOfCons += 1

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs
    val operator = mpConstraint.constraint.operator

    val constraint = underlyingSolver.makeConstraint()
    operator match {
      case ConstraintRelation.GE => constraint.setLb(-lhs.constant)
      case ConstraintRelation.LE => constraint.setUb(-lhs.constant)
      case ConstraintRelation.EQ => constraint.setBounds(-lhs.constant, -lhs.constant)
    }

    val iterator = lhs.terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      val indexes = decode(iterator.key)
      if (indexes.length == 1) constraint.setCoefficient(variable(indexes.head), iterator.value())
      else throw new IllegalArgumentException("Invalid constraint " + mpConstraint)
    }
  }

  /**
   * Solve the problem.
   *
   * @return status code indicating the nature of the solution
   */
  def solve(preSolve: PreSolve = PreSolve.DISABLED): SolutionStatus = {

    _solutionStatus = underlyingSolver.solve() match {

      case ResultStatus.OPTIMAL =>
        _solution = underlyingSolver.variables().map(_.solutionValue())
        _objectiveValue = Some(solverObjective.value())
        SolutionStatus.OPTIMAL

      case ResultStatus.FEASIBLE =>
        _solution = underlyingSolver.variables().map(_.solutionValue())
        _objectiveValue = Some(solverObjective.value())
        SolutionStatus.SUBOPTIMAL

      case ResultStatus.INFEASIBLE => SolutionStatus.INFEASIBLE

      case ResultStatus.UNBOUNDED => SolutionStatus.UNBOUNDED

      case _ =>
        logger.info(s"$solverId cannot handle the problem. Status was set to NOT_SOLVED.")
        SolutionStatus.NOT_SOLVED
    }

    _solutionStatus
  }

  /** Release the memory of this solver */
  def release(): Unit = underlyingSolver.delete()

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int): Unit = {
    require(0 <= limit)
    underlyingSolver.setTimeLimit(limit.toLong)
  }
}
