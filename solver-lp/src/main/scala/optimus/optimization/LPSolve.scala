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

import lpsolve.LpSolve
import optimus.algebra.ExpressionType.{ GENERIC, QUADRATIC }
import optimus.algebra._
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

/**
  * LpSolve solver.
  */
final class LPSolve extends MPSolver {

  type Solver = LpSolve

  protected var _objectiveConstant: Double = 0
  protected var underlyingSolver: Solver = LpSolve.makeLp(0, 0)

  /**
    * Problem builder, should configure the solver and append
    * mathematical model variables and constraints.
    *
    * @param numberOfVars number of variables in the model
    */
  def buildModel(numberOfVars: Int): Unit = {

    logger.info {
      "\n" +
        """  ______________________     ______            """ + "\n" +
        """  ___  /___  __ \_  ___/________  /__   ______ """ + "\n" +
        """  __  / __  /_/ /____ \_  __ \_  /__ | / /  _ \""" + "\n" +
        """  _  /___  ____/____/ // /_/ /  / __ |/ //  __/""" + "\n" +
        """  /_____/_/     /____/ \____//_/  _____/ \___/ """ + "\n"
    }

    this.numberOfVars = numberOfVars
    underlyingSolver = LpSolve.makeLp(0, numberOfVars)
    underlyingSolver.setInfinite(Double.MaxValue)
    underlyingSolver.setAddRowmode(true)
    underlyingSolver.setVerbose(LpSolve.IMPORTANT)
  }

  /**
    * Get value of the variable in the specified position. Solution
    * should exist in order for a value to exist.
    *
    * @param colId position of the variable
    * @return the value of the variable in the solution
    */
  def getVarValue(colId: Int): Double = {
    if (_solution.isEmpty || colId < 0 || colId >= numberOfVars) 0
    else solution(colId)
  }

  /**
    * Set bounds of variable in the specified position.
    *
    * @param colId position of the variable
    * @param lower domain lower bound
    * @param upper domain upper bound
    */
  def setBounds(colId: Int, lower: Double, upper: Double): Unit = {
    underlyingSolver.setBounds(colId + 1, lower, upper)
  }

  /**
    * Set upper bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundUpperBound(colId: Int): Unit = {
    underlyingSolver.setUpbo(colId + 1, underlyingSolver.getInfinite)
  }

  /**
    * Set lower bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundLowerBound(colId: Int): Unit = {
    underlyingSolver.setLowbo(colId + 1, -underlyingSolver.getInfinite)
  }

  /**
    * Set the column/variable as an integer variable
    *
    * @param colId position of the variable
    */
  def setInteger(colId: Int): Unit = {
    underlyingSolver.setInt(colId + 1, true)
  }

  /**
    * Set the column / variable as an binary integer variable
    *
    * @param colId position of the variable
    */
  def setBinary(colId: Int): Unit = {
    underlyingSolver.setBinary(colId + 1, true)
  }

  /**
    * Set the column/variable as a float variable
    *
    * @param colId position of the variable
    */
  def setFloat(colId: Int): Unit = {
    underlyingSolver.setInt(colId + 1, false)
  }

  /**
    * Add objective expression to be optimized by the solver.
    *
    * @param objective the expression to be optimized
    * @param minimize flag for minimization instead of maximization
    */
  def setObjective(objective: Expression, minimize: Boolean): Unit = {

    if (objective.getOrder == QUADRATIC || objective.getOrder == GENERIC)
      throw new IllegalArgumentException(s"LPSolve can handle only linear expressions and $objective is of higher order!")

    val indexes = objective.terms.keys.map(code => decode(code).head + 1)
    underlyingSolver.setObjFnex(objective.terms.size, objective.terms.values, indexes)
    _objectiveConstant = objective.constant

    if (!minimize) underlyingSolver.setMaxim()
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

    val LPOperator = operator match {
      case ConstraintRelation.GE => LpSolve.GE
      case ConstraintRelation.LE => LpSolve.LE
      case ConstraintRelation.EQ => LpSolve.EQ
    }

    val indexes = lhs.terms.keys.map(code => decode(code).head + 1)
    underlyingSolver.addConstraintex(
      lhs.terms.size,
      lhs.terms.values,
      indexes,
      LPOperator,
      -lhs.constant
    )
  }

  /**
    * Solve the problem.
    *
    * @return status code indicating the nature of the solution
    */
  def solve(preSolve: PreSolve = PreSolve.DISABLED): SolutionStatus = {

    if (preSolve == PreSolve.CONSERVATIVE)
      underlyingSolver.setPresolve(LpSolve.PRESOLVE_ROWS + LpSolve.PRESOLVE_COLS, 0)
    else if (preSolve == PreSolve.AGGRESSIVE)
      underlyingSolver.setPresolve(LpSolve.PRESOLVE_ROWS + LpSolve.PRESOLVE_COLS + LpSolve.PRESOLVE_LINDEP, 0)

    underlyingSolver.setAddRowmode(false)

    _solutionStatus = underlyingSolver.solve match {

      case LpSolve.OPTIMAL =>
        _solution = Array.tabulate(numberOfVars)(c => underlyingSolver.getVarPrimalresult(numberOfCons + c + 1))
        _objectiveValue = Some(underlyingSolver.getObjective + _objectiveConstant)
        SolutionStatus.OPTIMAL

      case LpSolve.SUBOPTIMAL =>
        _solution = Array.tabulate(numberOfVars)(c => underlyingSolver.getVarPrimalresult(numberOfCons + c + 1))
        _objectiveValue = Some(underlyingSolver.getObjective + _objectiveConstant)
        SolutionStatus.SUBOPTIMAL

      case LpSolve.INFEASIBLE =>
        SolutionStatus.INFEASIBLE

      case LpSolve.UNBOUNDED =>
        SolutionStatus.UNBOUNDED

      case LpSolve.TIMEOUT =>
        logger.warn("LPSolve timed out before solution was reached!")
        SolutionStatus.NOT_SOLVED

      case _ =>
        logger.info("LPSolve cannot handle the problem. Status was set to INFEASIBLE.")
        SolutionStatus.INFEASIBLE
    }

    _solutionStatus
  }

  /**
    * Release the memory of this solver
    */
  def release(): Unit = {
    underlyingSolver.deleteLp()
  }

  /**
    * Set a time limit for solver optimization. After the limit
    * is reached the solver stops running.
    *
    * @param limit the time limit
    */
  def setTimeout(limit: Int): Unit = {
    require(0 <= limit)
    underlyingSolver.setTimeout(limit)
  }
}
