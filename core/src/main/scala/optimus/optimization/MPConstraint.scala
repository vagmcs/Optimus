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
import optimus.algebra.{Constraint, ConstraintRelation, decode}
import scala.util.{Failure, Success, Try}

/**
  * Mathematical programming constraint in the problem.
  *
  * @param problem the problem in which the constraint belongs
  * @param constraint the constraint object
  * @param index the index of the constraint in the problem
  */
class MPConstraint(val problem: MPModel, val constraint: Constraint, val index: Int) extends StrictLogging {

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
