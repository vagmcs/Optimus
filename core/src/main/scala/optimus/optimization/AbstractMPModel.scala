package optimus.optimization

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

import gnu.trove.map.hash.TLongDoubleHashMap
import optimus.algebra._
import optimus.optimization.PreSolve.PreSolve
import optimus.optimization.ProblemStatus.ProblemStatus
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Success, Failure, Try}

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
    print("Added " + len + " constraints")
  }

  /**
    * Solve the problem.
    *
    * @param preSolve pre-solving mode
    * @return status code indicating the nature of the solution
    */
  def solveProblem(preSolve: PreSolve = PreSolve.DISABLE): ProblemStatus

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
  protected var solution = mutable.HashMap.empty[Int, Double]
  protected var objective: Expression = null
  protected var minimize = false
  protected var reOptimize = false

  protected lazy val solver: AbstractMPSolver = instantiateSolver()

  protected var status = ProblemStatus.NOT_SOLVED

  protected def instantiateSolver(): AbstractMPSolver

  // Register a variables to this problem and return a index for it
  def register(variable: MPVariable) = {
    variables += variable
    variables.length - 1
  }

  def variable(i: Int) = variables(i)

  // Set given variable bounds
  protected def setVarBounds(variable: MPVariable) = {
    if (variable.isUnbounded) {
      solver.setUnboundUpperBound(variable.index)
      solver.setUnboundLowerBound(variable.index)
    }
    else solver.setBounds(variable.index, variable.lowerBound, variable.upperBound)
  }

  protected def setVariableProperties() = {
    variables.foreach(setVarBounds)
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

  def getValue(varIndex: Int): Option[Double] = solution.get(varIndex)

  protected def optimize(expression: Expression, minimize: Boolean): AbstractMPProblem = {
    reOptimize = false
    objective = expression
    this.minimize = minimize
    this
  }

  def minimize(expression: Expression): AbstractMPProblem = optimize(expression, minimize = true)

  def maximize(expression: Expression): AbstractMPProblem = optimize(expression, minimize = false)

  def start(timeLimit: Int = Int.MaxValue, preSolve: PreSolve = PreSolve.DISABLE): Boolean = {

    if(!reOptimize) {
      solver.buildProblem(constraints.size, variables.size)

      println("Configuring variable bounds...")
      setVariableProperties()

      println("Adding objective function...")
      solver.addObjective(objective, minimize)

      print("Creating constraints: ")
      val start = System.currentTimeMillis()
      addAllConstraints()
      println(" in " + (System.currentTimeMillis() - start) + "ms")

      reOptimize = true
    }
    else println("Re-optimize")

    if (timeLimit < Int.MaxValue)
      solver.setTimeout(timeLimit)

    solveProblem(preSolve)
    (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL)
  }

  def solveProblem(preSolve: PreSolve) {
    println("Solving...")
    status = solver.solveProblem(preSolve)
    if ( (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL) )
      variables.indices foreach { i => solution(i) = solver.getValue(i) }

    println("Solution status is " + status)
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
                         override val symbol: String = ANONYMOUS) extends Var(symbol) {

  val index = problem.register(this)

  // A variable alone has a coefficient value of 1 in front of her
  val terms = new TLongDoubleHashMap()
  terms.put(encode(index), 1.0)

  protected var integer = false

  protected var binary = false

  protected var unbounded = doubleUnbounded

  /**
    * @return the value of the variable (integer rounded if the variable is integer).
    */
  def value = problem.getValue(index)

  /**
    * @return true if the variable is integer, false otherwise.
    */
  def isInteger = integer

  /**
    * @return true if the variable is a binary integer variable (e.g. 0-1).
    */
  def isBinary = binary

  /**
    * @return true if the variable is unbounded, false otherwise.
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

  def check(tol: Double = 10e-6): Boolean = slack match {
    case Success(value) => constraint.operator match {
      case ConstraintRelation.GE => value + tol >= 0
      case ConstraintRelation.LE => value + tol >= 0
      case ConstraintRelation.EQ => value.abs - tol <= 0
    }
    case Failure(exception) =>
      println(exception.getMessage)
      false
  }

  def slack: Try[Double] = {
    var res = 0.0

    val iteratorLHS = constraint.lhs.terms.iterator()
    while(iteratorLHS.hasNext) {
      iteratorLHS.advance()
      res += iteratorLHS.value * decode(iteratorLHS.key).map { v =>
        problem.getValue(v).getOrElse(return Failure(new NoSuchElementException(s"Value for variable ${problem.variable(v)} not found!")))
      }.product
    }

    val iteratorRHS = constraint.rhs.terms.iterator()
    while(iteratorRHS.hasNext) {
      iteratorRHS.advance()
      res -= iteratorRHS.value * decode(iteratorRHS.key).map{ v =>
        problem.getValue(v).getOrElse(return Failure(new NoSuchElementException(s"Value for variable ${problem.variable(v)} not found!")))
      }.product
    }

    val c = constraint.rhs.constant - constraint.lhs.constant
    constraint.operator match {
      case ConstraintRelation.GE => Success(res - c)
      case ConstraintRelation.LE => Success(c - res)
      case ConstraintRelation.EQ => Success(c - res)
    }
  }

  def isTight(tol: Double = 10e-6) = slack match {
    case Success(value) => value.abs <= tol
    case Failure(exception) =>
      println(exception.getMessage)
      false
  }

  override def toString = constraint.toString
}