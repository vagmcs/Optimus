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
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus.optimization

import optimus.algebra.ExpressionType.GENERIC
import optimus.algebra._
import optimus.algebra.{ConstraintRelation, Expression}
import org.ojalgo.constant.BigMath
import org.ojalgo.optimisation.{ExpressionsBasedModel, Optimisation, Variable}
import optimus.optimization.model.INFINITE
import optimus.optimization.enums.{PreSolve, SolutionStatus}
import optimus.optimization.model.MPConstraint

/**
  * oj solver.
  */
final class oJSolver extends MPSolver {

  type Solver = ExpressionsBasedModel

  protected var _objectiveConstant: Double = 0
  protected var underlyingSolver: Solver = new ExpressionsBasedModel

  // Internal flag for keeping optimization state
  private var minimize = true

  /**
    * Problem builder, should configure the solver and append
    * mathematical model variables and constraints.
    *
    * @param numberOfVars number of variables in the model
    */
  def buildModel(numberOfVars: Int): Unit = {

    logger.info { "\n" +
      """        _________      ______               """ + "\n" +
      """  ____________  /_____ ___  /______ ______  """ + "\n" +
      """  _  __ \__ _  /_  __  /_  /__  __  /  __ \ """ + "\n" +
      """  / /_/ / /_/ / / /_/ /_  / _  /_/ // /_/ / """ + "\n" +
      """  \____/\____/  \__._/ /_/  _\__. / \____/  """ + "\n" +
      """                            /____/          """ + "\n"
    }

    this.numberOfVars = numberOfVars

    var i = 0
    while (i < numberOfVariables) {
      underlyingSolver.addVariable(Variable.make(i.toString))
      i += 1
    }
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
  def setBounds(colId: Int, lower: Double, upper: Double): Unit = {
    if (upper == INFINITE) underlyingSolver.getVariable(colId).upper(null)
    else underlyingSolver.getVariable(colId).upper(upper)

    if (lower == INFINITE) underlyingSolver.getVariable(colId).lower(null)
    else underlyingSolver.getVariable(colId).lower(lower)
  }

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int): Unit = {
    underlyingSolver.getVariable(colId).upper(null)
  }

  /**
   * Set upper bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int): Unit = {
    underlyingSolver.getVariable(colId).lower(null)
  }

  /**
   * Set the column/variable as an integer variable
   *
   * @param colId position of the variable
   */
  def setInteger(colId: Int): Unit = {
    underlyingSolver.getVariable(colId).integer(true)
  }

  /**
   * Set the column / variable as an binary integer variable
   *
   * @param colId position of the variable
   */
  def setBinary(colId: Int): Unit = {
    underlyingSolver.getVariable(colId).binary()
  }

  /**
   * Set the column/variable as a float variable
   *
   * @param colId position of the variable
   */
  def setFloat(colId: Int): Unit = {
    underlyingSolver.getVariable(colId).integer(false)
  }

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def setObjective(objective: Expression, minimize: Boolean): Unit = {

    if (objective.getOrder == GENERIC)
      throw new IllegalArgumentException("ojSolver cannot handle expressions of higher order!")

    val objectiveFunction = underlyingSolver.addExpression()
    objectiveFunction.weight(BigMath.ONE)

    val iterator = objective.terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      val indexes = decode(iterator.key)
      if (indexes.length == 1) objectiveFunction
        .set(underlyingSolver.getVariable(indexes.head), iterator.value)
      else objectiveFunction
        .set(underlyingSolver.getVariable(indexes.head), underlyingSolver.getVariable(indexes(1)), iterator.value)
    }
    _objectiveConstant = objective.constant

    if (!minimize) this.minimize = false else this.minimize = true
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

    val constraint = underlyingSolver.addExpression()

    val iterator = lhs.terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      val indexes = decode(iterator.key)
      if (indexes.length == 1) constraint
        .set(underlyingSolver.getVariable(indexes.head), iterator.value)
      else constraint
        .set(underlyingSolver.getVariable(indexes.head), underlyingSolver.getVariable(indexes(1)), iterator.value)
    }

    operator match {
      case ConstraintRelation.GE => constraint.lower(-lhs.constant)
      case ConstraintRelation.LE => constraint.upper(-lhs.constant)
      case ConstraintRelation.EQ => constraint.level(-lhs.constant)
    }
  }

  /**
   * Solve the problem.
   *
   * @return status code indicating the nature of the solution
   */
  def solve(preSolve: PreSolve = PreSolve.DISABLED): SolutionStatus = {

    if (preSolve != PreSolve.DISABLED)
      logger.warn("ojSolver does not support pre-solving!")

    val result =
      if (this.minimize) underlyingSolver.minimise()
      else underlyingSolver.maximise()

    _solutionStatus = result.getState match {

      case Optimisation.State.OPTIMAL | Optimisation.State.DISTINCT =>
        _solution = Array.tabulate(numberOfVars)(col => result.get(col).doubleValue)
        _objectiveValue = Some(result.getValue + _objectiveConstant)
        SolutionStatus.OPTIMAL

      case Optimisation.State.INFEASIBLE =>
        SolutionStatus.INFEASIBLE

      case Optimisation.State.UNBOUNDED =>
        SolutionStatus.UNBOUNDED

      case _ =>
        _solution = Array.tabulate(numberOfVars)(col => result.get(col).doubleValue)
        SolutionStatus.SUBOPTIMAL
    }

    _solutionStatus
  }

  /**
   * Release the memory of this solver
   */
  def release(): Unit = {
    underlyingSolver.dispose()
  }

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int): Unit = {
    require(0 <= limit)
    underlyingSolver.options.time_abort = limit
  }
}
