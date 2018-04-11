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

package optimus.algebra

/**
  * A constraint has the form (expression RELATION expression). RELATION can be
  * one of the {<=, =, >=}.
  *
  * @param lhs left hand side expression
  * @param operator relation operator
  * @param rhs right hand side expression
  */
case class Constraint(lhs: Expression, operator: ConstraintRelation, rhs: Expression) {

  override def toString: String = s"$lhs $operator $rhs"

  /**
    * @param obj an object to compare
    * @return true in case this object has identical constant
    *         and terms as the obj argument; false otherwise.
    */
  override def equals(obj: Any): Boolean = obj match {
    case that: Constraint =>

      // Move terms in the left hand side of the expression in order to properly check equality.
      val (a, b) = (lhs - rhs, that.lhs - that.rhs)

      operator == that.operator && (a == b || -a == b || a == -b || -a == -b)
    case _ => false
  }
}