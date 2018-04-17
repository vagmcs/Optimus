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

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra._
import optimus.common.Measure._
import optimus.optimization.enums.PreSolve.DISABLED
import optimus.optimization.enums.{PreSolve, SolutionStatus, SolverLib}
import optimus.optimization.model.{MPConstraint, MPVar}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/**
  * Defines the mathematical programming model we are about to solve.
  *
  * @param solverLib a solver library (default is ojSolver)
  */
case class MPModel(solverLib: SolverLib = SolverLib.oJSolver) extends StrictLogging {

  protected val variables: ArrayBuffer[MPVar] = ArrayBuffer.empty[MPVar]
  protected val constraints: ArrayBuffer[MPConstraint] = ArrayBuffer.empty[MPConstraint]
  protected var solution = mutable.HashMap.empty[Int, Double]
  protected var objective: Expression = 0
  protected var minimize = false
  protected var reOptimize = false

  protected var solver: MPSolver = SolverFactory.instantiate(solverLib)

  /**
    * Register a variable to the model
    *
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param variable an MPVar to register
    * @return the index of the variable
    */
  def register(variable: MPVar): Int = {
    variables += variable
    variables.length - 1
  }

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param idx the index of the variable
    * @return the MPVar on the given index
    */
  def variable(idx: Int): Try[MPVar] = Try {
    variables(idx)
  }

  /**
    * @param idx the index of the variable
    * @return the solution value for the variable
    */
  def getVarValue(idx: Int): Option[Double] =
    solution.get(idx)


  /**
    * @see [[optimus.algebra.Constraint]]
    *
    * @param constraint a constraint to add
    * @return an MPConstraint
    */
  def add(constraint: Constraint): MPConstraint = {
    val constraintToAdd = MPConstraint(constraint, constraints.size, this)
    constraints += constraintToAdd
    if (reOptimize) solver.addConstraint(constraintToAdd)
    constraintToAdd
  }

  /**
    * @return the objective value of the underlying solver
    */
  def objectiveValue: Double = solver.objectiveValue.get

  /**
    * @param expression
    * @return
    */
  def minimize(expression: Expression): MPModel = optimize(expression, minimize = true)

  /**
    * @param expression
    * @return
    */
  def maximize(expression: Expression): MPModel = optimize(expression, minimize = false)

  protected def optimize(expression: Expression, minimize: Boolean): MPModel = {
    reOptimize = false
    objective = expression
    this.minimize = minimize
    this
  }

  def start(timeLimit: Int = Int.MaxValue, preSolve: PreSolve = DISABLED): Boolean = {

    if (!reOptimize) {
      solver.buildModel(variables.size)

      logger.info("Configuring variables...")
      for (x <- variables) {
        if (x.isUnbounded) solver.setDoubleUnbounded(x.index)
        else solver.setBounds(x.index, x.lowerBound, x.upperBound)

        if (x.isBinary) solver.setBinary(x.index)
        else if(x.isInteger) solver.setInteger(x.index)
      }

      measureTime("Objective function added in: ") {
        solver.setObjective(objective, minimize)
      }

      measureTime("Constraints created in: ") {
        solver.addAllConstraints(constraints)
      }

      reOptimize = true
    }
    else logger.info("Re-optimize")

    if (timeLimit < Int.MaxValue)
      solver.setTimeout(timeLimit)

    logger.info("Solving...")
    val status = solver.solve(preSolve)

    if ( (status == SolutionStatus.OPTIMAL) || (status == SolutionStatus.SUBOPTIMAL) )
      variables.indices foreach { i => solution(i) = solver.getVarValue(i) }

    logger.info("Solution status is " + status)

    //val status = solveProblem(preSolve)
    (status == SolutionStatus.OPTIMAL) || (status == SolutionStatus.SUBOPTIMAL)
  }

  def solveProblem(preSolve: PreSolve): SolutionStatus = {
    logger.info("Solving...")
    val status = solver.solve(preSolve)

    if ( (status == SolutionStatus.OPTIMAL) || (status == SolutionStatus.SUBOPTIMAL) )
      variables.indices foreach { i => solution(i) = solver.getVarValue(i) }

    logger.info("Solution status is " + status)
    status
  }

  /**
    * @param tol
    * @return
    */
  def checkConstraints(tol: Double = 10e-6): Boolean = constraints.forall(_.check(tol))

  /**
    * @return
    */
  def getStatus: SolutionStatus = solver.solutionStatus

  /**
    *
    */
  def release(): Unit = solver.release()
}