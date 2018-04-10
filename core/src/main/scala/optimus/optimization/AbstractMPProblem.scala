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