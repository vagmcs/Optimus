package optimus.lqprog

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

import mosek._
import optimus.algebra.Expression
import optimus.lqprog.ProblemStatus.ProblemStatus

/**
 * Mosek solver.
 *
 * @author Vagelis Michelioudakis
 */
final class Mosek extends AbstractMPSolver {

  var nbRows = 0
  var nbCols = 0
  var solution = Array[Double]()
  var objectiveValue = 0.0
  var status = ProblemStatus.NOT_SOLVED

  var env = new Env()
  val task = new Task(env, 0, 0)
  task.set_Stream(mosek.Env.streamtype.log, new Stream { def stream(msg: String) = print(msg) })

  /**
   * Problem builder, should configure the solver and append
   * mathematical model variables.
   *
   * @param nbRows rows in the model
   * @param nbCols number of variables in the model
   */
  def buildProblem(nbRows: Int, nbCols: Int) = {
    this.nbRows = nbRows
    this.nbCols = nbCols

    task.appendvars(nbCols)
    task.appendcons(nbRows)
  }

  /**
   * Get value of the variable in the specified position. Solution
   * should exist in order for a value to exist.
   *
   * @param colId position of the variable
   * @return the value of the variable in the solution
   */
  def getValue(colId: Int): Double = solution(colId)

  /**
   * Set bounds of variable in the specified position.
   *
   * @param colId position of the variable
   * @param lower domain lower bound
   * @param upper domain upper bound
   */
  def setBounds(colId: Int, lower: Double, upper: Double) = {
    task.putvarbound(colId, Env.boundkey.ra, lower, upper)
  }

  /**
   * Set lower bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundUpperBound(colId: Int) = {
    task.putvarbound(colId, Env.boundkey.lo, 0, Double.PositiveInfinity)
  }

  /**
   * Set upper bound to unbounded (infinite)
   *
   * @param colId position of the variable
   */
  def setUnboundLowerBound(colId: Int) = {
    task.putvarbound(colId, Env.boundkey.up, 0, Double.PositiveInfinity)
  }

  /**
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def addObjective(objective: Expression, minimize: Boolean) = ???

  /**
   * Add a mathematical programming constraint to the solver.
   *
   * @param mpConstraint the mathematical programming constraint
   */
  def addConstraint(mpConstraint: MPConstraint) = ???

  /**
   * Solve the problem.
   *
   * @return status code indicating the nature of the solution
   */
  def solveProblem(): ProblemStatus = ???

  /**
   * Release the memory of this solver
   */
  def release() = {
    task.dispose()
    env.dispose()
  }

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int) = {
    require(0 <= limit)
    task.putdouparam(Env.dparam.optimizer_max_time, limit)
  }

  def convert(mpConstraint: MPConstraint): IntegerRepresentation = {

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs

    var aSub = Array.empty[Int]
    var aVal = Array.empty[Double]
    var qSubI = Array.empty[Int]
    var qSubJ = Array.empty[Int]
    var qVal = Array.empty[Double]

    lhs.terms.foreach { term =>
      term._1.length match {
        case 1 =>
          aSub :+= term._1(0).index
          aVal :+= term._2
        case 2 =>
          qSubI :+= Math.max(term._1(0).index, term._1(1).index)
          qSubJ :+= Math.min(term._1(0).index, term._1(1).index)
          qVal :+= term._2
        case _ => throw new IllegalArgumentException("Higher order expression " + mpConstraint)
      }
    }

    new IntegerRepresentation(aSub, aVal, qSubI, qSubJ, qVal, -lhs.constant)
  }

  protected final class IntegerRepresentation(val aSub: Array[Int], val aVal: Array[Double], val qSubI: Array[Int], val qSubJ: Array[Int],
                                            val qVal: Array[Double], val rhs: Double)
}
