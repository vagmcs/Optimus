package optimus.optimization

import lpsolve.LpSolve
import optimus.algebra._
import optimus.optimization.PreSolve.PreSolve
import optimus.optimization.ProblemStatus.ProblemStatus

/*
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

/**
 * LPSolve solver.
 *
 * @author Vagelis Michelioudakis
 */
final class LPSolve extends AbstractMPSolver {

  var lp: LpSolve = null
  var nbRows = 0
  var nbCols = 0
  var solution = Array[Double]()
  var objectiveValue = 0.0
  var status = ProblemStatus.NOT_SOLVED

  /**
   * Problem builder, should configure the solver and append
   * mathematical model variables.
   *
   * @param nbRows rows in the model
   * @param nbCols number of variables in the model
   */
  def buildProblem(nbRows: Int, nbCols: Int) = {

    println {
        """  ______________________     ______            """ + "\n" +
        """  ___  /___  __ \_  ___/________  /__   ______ """ + "\n" +
        """  __  / __  /_/ /____ \_  __ \_  /__ | / /  _ \""" + "\n" +
        """  _  /___  ____/____/ // /_/ /  / __ |/ //  __/""" + "\n" +
        """  /_____/_/     /____/ \____//_/  _____/ \___/ """ + "\n"
    }

    println("Model lpSolve: " + nbRows + "x" + nbCols)

    this.nbRows = 0
    this.nbCols = nbCols

    lp = LpSolve.makeLp(0, nbCols)
    lp.setInfinite(Double.MaxValue)
    lp.setAddRowmode(true)
    lp.setVerbose(LpSolve.IMPORTANT)
  }

  /**
   * Get value of the variable in the specified position. Solution
   * should exist in order for a value to exist.
   *
   * @param colId position of the variable
   * @return the value of the variable in the solution
   */
  def getValue(colId: Int): Double = {
    if (solution == null || colId < 0 || colId >= nbCols) 0.0
    else solution(colId)
  }

  /**
   * Set bounds of variable in the specified position.
   *
   * @param colId position of the variable
   * @param lower domain lower bound
   * @param upper domain upper bound
   */
  def setBounds(colId: Int, lower: Double, upper: Double) = {
    lp.setBounds(colId + 1, lower, upper)
  }

  /**
   * Set upper bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int) = {
    lp.setUpbo(colId + 1, lp.getInfinite)
  }

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int) = {
    lp.setLowbo(colId + 1, -lp.getInfinite)
  }

  /**
   * Set the column/variable as an integer variable
   *
   * @param colId position of the variable
   */
  def setInteger(colId: Int) {
    lp.setInt(colId + 1, true)
  }

  /**
   * Set the column / variable as an binary integer variable
   *
   * @param colId position of the variable
   */
  def setBinary(colId: Int) {
    lp.setBinary(colId + 1, true)
  }

  /**
   * Set the column/variable as a float variable
   *
   * @param colId position of the variable
   */
  def setFloat(colId: Int) {
    lp.setInt(colId + 1, false)
  }

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def addObjective(objective: Expression, minimize: Boolean) = {

    if(objective.getOrder == ExpressionOrder.QUADRATIC || objective.getOrder == ExpressionOrder.HIGHER )
        throw new IllegalArgumentException("LPSolve can handle only linear expressions and " + objective + " is higher order!")

    val list = objective.terms.toList
    lp.setObjFnex(objective.terms.size, list.map(pair => pair._2).toArray, list.map(pair => pair._1.head.index + 1).toArray)
    if (!minimize) lp.setMaxim()
  }

  /**
   * Add a mathematical programming constraint to the solver.
   *
   * @param mpConstraint the mathematical programming constraint
   */
  def addConstraint(mpConstraint: MPConstraint) = {
    nbRows += 1

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs
    val list = lhs.terms.toList
    val operator = mpConstraint.constraint.operator

    val LPOperator = operator match {
      case ConstraintRelation.GE => LpSolve.GE
      case ConstraintRelation.LE => LpSolve.LE
      case ConstraintRelation.EQ => LpSolve.EQ
    }

    lp.addConstraintex(lhs.terms.size, list.map(pair => pair._2).toArray,
                      list.map(pair => pair._1.head.index + 1).toArray, LPOperator, -lhs.constant)
    lp.setRowName(nbRows, "")
  }

  /**
   * Solve the problem.
   *
   * @return status code indicating the nature of the solution
   */
  def solveProblem(preSolve: PreSolve = PreSolve.DISABLE): ProblemStatus = {

    if (preSolve == PreSolve.CONSERVATIVE) lp.setPresolve(LpSolve.PRESOLVE_ROWS + LpSolve.PRESOLVE_COLS, 0)
    else if (preSolve == PreSolve.AGGRESSIVE) lp.setPresolve(LpSolve.PRESOLVE_ROWS + LpSolve.PRESOLVE_COLS + LpSolve.PRESOLVE_LINDEP, 0)
    
    lp.setAddRowmode(false)

    lp.solve match {

      case LpSolve.OPTIMAL =>
        solution = Array.tabulate(nbCols)(c => lp.getVarPrimalresult(nbRows + c + 1))
        objectiveValue = lp.getObjective
        ProblemStatus.OPTIMAL

      case LpSolve.SUBOPTIMAL =>
        solution = Array.tabulate(nbCols)(c => lp.getVarPrimalresult(nbRows + c + 1))
        objectiveValue = lp.getObjective
        ProblemStatus.SUBOPTIMAL

      case LpSolve.INFEASIBLE =>
        ProblemStatus.INFEASIBLE

      case LpSolve.UNBOUNDED =>
        ProblemStatus.UNBOUNDED

      case LpSolve.TIMEOUT =>
        println("LPSolve timed out before solution was reached!")
        ProblemStatus.NOT_SOLVED

      case _ =>
        ProblemStatus.INFEASIBLE
    }
  }

  /**
   * Release the memory of this solver
   */
  def release() = {
    lp.deleteLp()
  }

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int) =  {
    require(0 <= limit)
    lp.setTimeout(limit)
  }
}
