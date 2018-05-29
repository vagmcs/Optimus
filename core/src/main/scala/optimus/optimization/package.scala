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

package optimus

import optimus.algebra.{ Constraint, Expression }
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

package object optimization {

  def add(constraint: Constraint)(implicit model: MPModel): MPConstraint = model.add(constraint)

  def subjectTo(constraints: Constraint*)(implicit model: MPModel): Unit = constraints.foreach(add)

  def start(
      preSolve: PreSolve = PreSolve.DISABLED,
      timeLimit: Int = Int.MaxValue)
    (implicit model: MPModel): Boolean = model.start(timeLimit, preSolve)

  def minimize(expression: Expression)(implicit model: MPModel): MPModel = model.minimize(expression)

  def maximize(expression: Expression)(implicit model: MPModel): MPModel = model.maximize(expression)

  def release()(implicit model: MPModel): Unit = model.release()

  def objectiveValue(implicit model: MPModel): Double = model.objectiveValue

  def status(implicit model: MPModel): SolutionStatus = model.getStatus

  def checkConstraints(tol: Double = 10e-6)(implicit model: MPModel): Boolean = model.checkConstraints(tol)
}
