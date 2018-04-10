/*
 *
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
 *  Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 *  This file is part of Optimus.
 *
 *  Optimus is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  Optimus is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 *  License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus.optimization

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra._
import optimus.optimization.enums.{PreSolve, ProblemStatus}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

/**
  * Should define the problem we are about to solve
  */
abstract class AbstractMPProblem extends StrictLogging {

  protected val variables = ArrayBuffer[MPVariable]()
  protected val constraints = ArrayBuffer[MPConstraint]()
  protected var solution = mutable.HashMap.empty[Int, Double]
  protected var objective: Expression = null
  protected var minimize = false
  protected var reOptimize = false

  protected lazy val solver: MPSolver = instantiateSolver()

  protected var status: ProblemStatus = ProblemStatus.NOT_SOLVED

  protected def instantiateSolver(): MPSolver

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

  def start(timeLimit: Int = Int.MaxValue, preSolve: PreSolve = PreSolve.DISABLED): Boolean = {

    if(!reOptimize) {
      solver.buildProblem(constraints.size, variables.size)

      logger.info("Configuring variable bounds...")
      setVariableProperties()

      logger.info("Adding objective function...")
      solver.setObjective(objective, minimize)

      logger.info("Creating constraints: ")
      val start = System.currentTimeMillis()
      addAllConstraints()
      logger.info(" in " + (System.currentTimeMillis() - start) + "ms")

      reOptimize = true
    }
    else logger.info("Re-optimize")

    if (timeLimit < Int.MaxValue)
      solver.setTimeout(timeLimit)

    solveProblem(preSolve)
    (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL)
  }

  def solveProblem(preSolve: PreSolve) {
    logger.info("Solving...")
    status = solver.solveProblem(preSolve)

    if ( (status == ProblemStatus.OPTIMAL) || (status == ProblemStatus.SUBOPTIMAL) )
      variables.indices foreach { i => solution(i) = solver.getValue(i) }

    logger.info("Solution status is " + status)
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
  override val terms = LongDoubleMap(this)

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
class MPConstraint(val problem: AbstractMPProblem, val constraint: Constraint, val index: Int) extends StrictLogging {

  def check(tol: Double = 10e-6): Boolean = slack match {
    case Success(value) => constraint.operator match {
      case ConstraintRelation.GE => value + tol >= 0
      case ConstraintRelation.LE => value + tol >= 0
      case ConstraintRelation.EQ => value.abs - tol <= 0
    }
    case Failure(exception) =>
      logger.error(exception.getMessage)
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
      logger.error(exception.getMessage)
      false
  }

  override def toString = constraint.toString
}