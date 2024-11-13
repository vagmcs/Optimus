/*
 *
 *   /\\\\\
 *  /\\\///\\\
 * /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 * /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 * \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *  \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *    \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *       \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *          \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * The mathematical programming library for Scala.
 *     
 */

package optimus.optimization

import mosek._
import optimus.algebra._
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint
import scala.util.{ Failure, Success, Try }
import optimus.optimization.model.INFINITE

final class Mosek extends MPSolver {

  type Solver = Task

  private val env = new Env
  protected var underlyingSolver: Solver = new Task(env)
  private var solutionType = soltype.itr

  /**
   * Problem builder, should configure the solver and append
   * mathematical model variables and constraints.
   *
   * @param numberOfVars number of variables in the model
   */
  def buildModel(numberOfVars: Int): Unit = {

    logger.info {
      "\n" +
        """     __  ___                __   """ + "\n" +
        """    /  |/  /___  ________  / /__ """ + "\n" +
        """   / /|_/ / __ \/ ___/ _ \/ //_/ """ + "\n" +
        """  / /  / / /_/ (__  )  __/ ,<    """ + "\n" +
        """ /_/  /_/\____/____/\___/_/|_|   """ + "\n"
    }

    this.numberOfVars = numberOfVars
    underlyingSolver.appendvars(numberOfVars)
  }

  /**
   * Get value of the variable in the specified position. Solution
   * should exist in order for a value to exist.
   *
   * @param colId position of the variable
   * @return the value of the variable in the solution
   */
  def getVarValue(colId: Int): Double = solution(colId)

  /**
   * Set bounds of variable in the specified position.
   *
   * @param colId position of the variable
   * @param lower domain lower bound
   * @param upper domain upper bound
   */
  def setBounds(colId: Int, lower: Double, upper: Double): Unit =
    if (upper == INFINITE) underlyingSolver.putvarbound(colId, boundkey.lo, lower, Double.MaxValue)
    else if (lower == INFINITE) underlyingSolver.putvarbound(colId, boundkey.up, Double.MinValue, upper)
    else underlyingSolver.putvarbound(colId, boundkey.ra, lower, upper)

  /**
   * Set bot upper and lower bounds to unbounded (infinite).
   *
   * @param colId position of the variable
   */
  override def setDoubleUnbounded(colId: Int): Unit =
    underlyingSolver.putvarbound(colId, boundkey.fr, Double.MinValue, Double.MaxValue)

  /**
   * Set upper bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int): Unit = underlyingSolver.putvarbound(colId, boundkey.lo, 0, Double.MaxValue)

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int): Unit = underlyingSolver.putvarbound(colId, boundkey.up, Double.MinValue, 0)

  /**
   * Set the column/variable as an integer variable
   *
   * @param colId position of the variable
   */
  def setInteger(colId: Int): Unit = underlyingSolver.putvartype(colId, variabletype.type_int)

  /**
   * Set the column / variable as an binary integer variable
   *
   * @param colId position of the variable
   */
  def setBinary(colId: Int): Unit = underlyingSolver.putvartype(colId, variabletype.type_int)

  /**
   * Set the column/variable as a float variable
   *
   * @param colId position of the variable
   */
  def setFloat(colId: Int): Unit = underlyingSolver.putvartype(colId, variabletype.type_cont)

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize  flag for minimization instead of maximization
   */
  def setObjective(objective: Expression, minimize: Boolean): Unit = {

    objective.getOrder match {
      case ExpressionType.GENERIC => throw new IllegalArgumentException("Higher than quadratic: " + objective)

      case ExpressionType.QUADRATIC =>
        val iterator = objective.terms.iterator
        while (iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if (indexes.length == 1) underlyingSolver.putcj(indexes.head, iterator.value)
          else if (indexes.head == indexes(1)) underlyingSolver.putqobjij(
            indexes.head,
            indexes.head,
            2 * iterator.value
          )
          else underlyingSolver.putqobjij(
            Math.max(indexes.head, indexes(1)),
            Math.min(indexes.head, indexes(1)),
            iterator.value
          )
        }
      case ExpressionType.LINEAR =>
        val variables = objective.terms.keys.map(code => decode(code).head)
        underlyingSolver.putclist(variables, objective.terms.values)

      case ExpressionType.CONSTANT =>
    }

    underlyingSolver.putcfix(objective.constant)
    underlyingSolver.putobjsense(if (minimize) objsense.minimize else objsense.maximize)
  }

  /**
   * Add a mathematical programming constraint to the solver.
   *
   * @param mpConstraint the mathematical programming constraint
   */
  def addConstraint(mpConstraint: MPConstraint): Unit = {

    underlyingSolver.appendcons(1)

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs
    val rhs = -lhs.constant
    val operator = mpConstraint.constraint.operator

    lhs.getOrder match {
      case ExpressionType.GENERIC => throw new IllegalArgumentException("Higher than quadratic: " + lhs)

      case ExpressionType.QUADRATIC =>
        var linearIndexes = Array.emptyIntArray
        var linearValues = Array.emptyDoubleArray
        var rowIndexes = Array.emptyIntArray
        var colsIndexes = Array.emptyIntArray
        var quadraticValues = Array.emptyDoubleArray

        val iterator = lhs.terms.iterator
        while (iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if (indexes.length == 1) {
            linearIndexes :+= indexes.head
            linearValues :+= iterator.value
          } else if (indexes.head == indexes(1)) {
            rowIndexes :+= indexes.head
            colsIndexes :+= indexes.head
            quadraticValues :+= 2 * iterator.value
          } else {
            rowIndexes :+= Math.max(indexes.head, indexes(1))
            colsIndexes :+= Math.min(indexes.head, indexes(1))
            quadraticValues :+= iterator.value
          }
        }
        underlyingSolver.putarow(numberOfCons, linearIndexes, linearValues)
        underlyingSolver.putqconk(numberOfCons, rowIndexes, colsIndexes, quadraticValues)

      case ExpressionType.LINEAR =>
        val variables = lhs.terms.keys.map(code => decode(code).head)
        underlyingSolver.putarow(numberOfCons, variables, lhs.terms.values)

      case ExpressionType.CONSTANT =>
    }

    operator match {
      case ConstraintRelation.GE => underlyingSolver.putconbound(numberOfCons, boundkey.lo, rhs, Double.MaxValue)
      case ConstraintRelation.LE => underlyingSolver.putconbound(numberOfCons, boundkey.up, Double.MinValue, rhs)
      case ConstraintRelation.EQ => underlyingSolver.putconbound(numberOfCons, boundkey.fx, rhs, rhs)
    }

    numberOfCons += 1
  }

  /**
   * Solve the problem.
   *
   * @param preSolve pre-solving mode
   * @return status code indicating the nature of the solution
   */
  def solve(preSolve: PreSolve): SolutionStatus = {

    if (preSolve != PreSolve.DISABLED) logger.warn("Mosek pre-solving is not currently supported!")

    _solution = Array.ofDim(numberOfVars)
    val optimizationStatus = underlyingSolver.optimize()

    _solutionStatus = optimizationStatus match {
      case rescode.ok =>
        val solutionStatus = new Array[solsta](1)
        Try(underlyingSolver.getsolsta(soltype.itr, solutionStatus)) match {
          case Success(_) =>
          case Failure(_) =>
            solutionType = soltype.itg
            underlyingSolver.getsolsta(soltype.itg, solutionStatus)
        }

        solutionStatus.head match {
          case solsta.optimal | solsta.integer_optimal =>
            underlyingSolver.getxx(solutionType, _solution)
            _objectiveValue = Some(underlyingSolver.getprimalobj(solutionType))
            SolutionStatus.OPTIMAL

          case solsta.prim_feas | solsta.dual_feas =>
            underlyingSolver.getxx(solutionType, _solution)
            _objectiveValue = Some(underlyingSolver.getprimalobj(solutionType))
            SolutionStatus.SUBOPTIMAL

          case solsta.prim_infeas_cer | solsta.dual_infeas_cer => SolutionStatus.INFEASIBLE

          case _ => SolutionStatus.NOT_SOLVED
        }
      case _ =>
        logger.error(s"Optimization failed with code = $optimizationStatus")
        SolutionStatus.NOT_SOLVED
    }

    _solutionStatus
  }

  /** Release the memory of this solver */
  def release(): Unit = {
    underlyingSolver.dispose()
    env.dispose()
  }

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int): Unit = underlyingSolver.putdouparam(dparam.optimizer_max_time, limit)
}
