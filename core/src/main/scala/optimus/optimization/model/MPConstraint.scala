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

package optimus.optimization.model

import com.typesafe.scalalogging.StrictLogging
import optimus.algebra.ConstraintRelation._
import optimus.algebra.{Constraint, decode}
import optimus.optimization.MPModel
import scala.util.{Failure, Success, Try}

/**
  * Mathematical programming model constraint.
  *
  * @param constraint the constraint expression
  * @param index the index of the constraint in the model
  * @param model the model that the constraint belongs
  */
case class MPConstraint(constraint: Constraint, index: Int, model: MPModel) extends StrictLogging {

  def check(tol: Double = 10e-6): Boolean = slack match {
    case Success(value) => constraint.operator match {
      case GE => value + tol >= 0
      case LE => value + tol >= 0
      case EQ => value.abs - tol <= 0
    }
    case Failure(exception) =>
      logger.error(exception.getMessage)
      false
  }

  def slack: Try[Double] = {
    var res = 0.0

    val iteratorLHS = constraint.lhs.terms.iterator
    while (iteratorLHS.hasNext) {
      iteratorLHS.advance()
      res += iteratorLHS.value * decode(iteratorLHS.key).map { v =>
        model.getVarValue(v).getOrElse(
          return Failure(new NoSuchElementException(s"Value for variable ${model.variable(v)} not found!"))
        )
      }.product
    }

    val iteratorRHS = constraint.rhs.terms.iterator
    while (iteratorRHS.hasNext) {
      iteratorRHS.advance()
      res -= iteratorRHS.value * decode(iteratorRHS.key).map{ v =>
        model.getVarValue(v).getOrElse(
          return Failure(new NoSuchElementException(s"Value for variable ${model.variable(v)} not found!"))
        )
      }.product
    }

    val c = constraint.rhs.constant - constraint.lhs.constant
    constraint.operator match {
      case GE => Success(res - c)
      case LE => Success(c - res)
      case EQ => Success(c - res)
    }
  }

  def isTight(tol: Double = 10e-6): Boolean = slack match {
    case Success(value) => value.abs <= tol
    case Failure(exception) =>
      logger.error(exception.getMessage)
      false
  }

  override def toString: String = constraint.toString
}
