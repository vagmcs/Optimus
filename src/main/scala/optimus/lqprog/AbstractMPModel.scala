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

import optimus.algebra._
import optimus.lqprog.ProblemStatus.ProblemStatus
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

/**
 * Abstract mathematical programming model.
 *
 * @author Vagelis Michelioudakis
 */

object ProblemStatus extends Enumeration {

  type ProblemStatus = Value

  val NOT_SOLVED = Value("Not solved yet")
  val OPTIMAL = Value("Optimal")
  val SUBOPTIMAL = Value("Suboptimal")
  val UNBOUNDED = Value("Unbounded")
  val INFEASIBLE = Value("Infeasible")
}

/**
 * Abstract class that should be extended to define a linear-quadratic solver.
 */
abstract class AbstractMPSolver {

  /**
   * Number of rows in the model
   */
  var nbRows: Int

  /**
   * Number of columns / variables in the model
   */
  var nbCols: Int

  /**
   * Solution, one entry for each column / variable
   */
  var solution: Array[Double]

  /**
   * Objective value
   */
  var objectiveValue: Double
  
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
   * Add objective expression to be optimized by the solver.
   *
   * @param objective the expression to be optimized
   * @param minimize flag for minimization instead of maximization
   */
  def addObjective(objective: Expression, minimize: Boolean)

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
    println("Added " + len + " constraints.")
  }

  /**
   * Solve the problem.
   *
   * @return status code indicating the nature of the solution
   */
  def solveProblem(): ProblemStatus

  /**
   * Release the memory of this solver
   */
  def release()

  /**
   * Set a time limit for solver optimization. After the limit
   * is reached the solver stops running.
   *
   * @param limit the time limit
   */
  def setTimeout(limit: Int)
}

/**
 * Should define the problem we are about to solve
 */
abstract class AbstractMPProblem {

  protected val variables = ArrayBuffer[MPVariable]()
  protected val constraints = ArrayBuffer[MPConstraint]()
  protected var solution = HashMap.empty[Int, Double]
  protected var objective: Expression = null
  protected var minimize = false
  protected var reOptimize = false

  protected val solver: AbstractMPSolver

  protected var status = ProblemStatus.NOT_SOLVED

  def register(variable: MPVariable) = {
    variables += variable
    variables.length - 1
  }

  def variable(i: Int) = variables(i)

  protected def setVarBounds(variable: MPVariable) = {
    if (variable.isUnbounded) {
      solver.setUnboundUpperBound(variable.index)
      solver.setUnboundLowerBound(variable.index)
    }
    else solver.setBounds(variable.index, variable.lowerBound, variable.upperBound)
  }

  protected def setVariableProperties() = {
    variables.foreach(v => setVarBounds(v))
  }

  def add(constraint: Constraint): MPConstraint = {
    val constraintToAdd = new MPConstraint(this, constraint, constraints.size)
    constraints += constraintToAdd
    if(reOptimize) solver.addConstraint(constraintToAdd)
    constraintToAdd
  }

  protected def addAllConstraints() = {
    solver.addAllConstraints(constraints)
  }

  def objectiveValue() = solver.objectiveValue

  def getValue(varIndex: Int): Option[Double] = Some(solution(varIndex))

  protected def optimize(expression: Expression, minimize: Boolean): AbstractMPProblem = {
    reOptimize = false
    objective = expression
    this.minimize = minimize
    this
  }

  def minimize(expression: Expression): AbstractMPProblem = optimize(expression, minimize = true)

  def maximize(expression: Expression): AbstractMPProblem = optimize(expression, minimize = false)

  def start(timeLimit: Int = Int.MaxValue): Boolean = {

    if(!reOptimize) {
      solver.buildProblem(constraints.size, variables.size)

      println("Configuring variable bounds...")
      setVariableProperties()

      println("Adding objective function...")
      solver.addObjective(objective, minimize)

      println("Creating constraints...")
      val start = System.currentTimeMillis()
      addAllConstraints()
      println("Time to add constraints:" + (System.currentTimeMillis() - start) + "ms")

      reOptimize = true
    }
    else println("Re-optimize")

    if (timeLimit < Int.MaxValue)
      solver.setTimeout(timeLimit)

    solveProblem()
    (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL)
  }

  def solveProblem() {
    println("Solving ...")
    status = solver.solveProblem()
    if ( (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL) )
      (0 until variables.length) foreach { i => solution(i) = solver.getValue(i) }
  }

  def checkConstraints(tol: Double = 10e-6): Boolean = constraints.forall(_.check(tol))

  def getStatus: ProblemStatus = status

  def release() = solver.release()
}

/**
 * Mathematical programming unbounded variable in the problem. The domain is defined
 * (0, +inf) if the variable is unbounded or (-inf, +inf) otherwise.
 *
 * @param problem the problem in which the variable belongs
 * @param lowerBound the lower bound in the domain
 * @param upperBound the upper bound in the domain
 * @param doubleUnbounded unbounded domain (-inf, +inf)
 * @param symbol the symbol of the variable (default is ANONYMOUS)
 *
 */
class MPVariable(val problem: AbstractMPProblem, val lowerBound: Double, val upperBound: Double, doubleUnbounded: Boolean,
                         override val symbol: String = Variable.ANONYMOUS) extends Variable(symbol) {

  val index = problem.register(this)

  protected var integer = false

  protected var binary = false

  protected var unbounded = doubleUnbounded

  /**
   * Returns the value of the variable (integer rounded if the variable is integer)
   */
  def value = problem.getValue(index)

  /**
   * Return true if the variable is integer, false otherwise
   */
  def isInteger = integer

  /**
   * @return true if the variable is a binary integer variable (e.g. 0-1)
   */
  def isBinary = binary

  /**
   * Return true if the variable is unbounded, false otherwise
   */
  def isUnbounded = unbounded

}

/**
 * Mathematical programming constraint in the problem.
 *
 * @param problem the problem in which the constraint belongs
 * @param constraint the constraint object
 * @param index the index of the constraint in the problem
 */
class MPConstraint(val problem: AbstractMPProblem, val constraint: Constraint, val index: Int) {

  def check(tol: Double = 10e-6): Boolean = constraint.operator match {
      case ConstraintRelation.GE => slack + tol >= 0
      case ConstraintRelation.LE => slack + tol >= 0
      case ConstraintRelation.EQ => slack.abs - tol <= 0
  }

  def slack: Double = {
    var res = 0.0

    for ( (variables, c) <- constraint.lhs.terms)
      res += c * variables.map(v => v.asInstanceOf[MPVariable].value.get).reduce(_ * _)

    for ( (variables, c) <- constraint.rhs.terms)
      res -= c * variables.map(v => v.asInstanceOf[MPVariable].value.get).reduce(_ * _)

    val c = constraint.rhs.constant - constraint.lhs.constant
    constraint.operator match {
      case ConstraintRelation.GE => res - c
      case ConstraintRelation.LE => c - res
      case ConstraintRelation.EQ => c - res
    }
  }

  def isTight(tol: Double = 10e-6) = slack.abs <= tol

  override def toString = constraint.toString
}