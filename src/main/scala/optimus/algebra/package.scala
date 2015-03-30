package optimus

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

/**
 * Helper function for summation and product of multiple expressions
 * stored in iterable data structures.
 *
 * @author Vagelis Michelioudakis
 * @author Anastasios Skarlatidis
 */
package object algebra {

  // functions over iterable data structures

  def sum(expressions : Iterable[Expression]) : Expression = {

    var temporal = scala.collection.mutable.Map[Vector[Variable], Double]()
    var tConstant = 0.0

    for (expr <- expressions) {
      tConstant += expr.constant
      for ((variables, coefficient) <- expr.terms) {
        temporal.get(variables) match {
          case Some(c) => temporal(variables) = c + coefficient
          case None => temporal += (variables -> coefficient)
        }
      }
    }
    temporal = temporal.filterNot(_._2 == 0)

    new Expression {
      val constant = tConstant
      val terms = temporal.toMap
    }
  }

  // These functions produce mathematical expressions over joint iterable and then summing out the results

  def sum[A](indexes: Iterable[A])(f : A => Expression) : Expression = sum(indexes map f)

  def sum[A, B](indexesA: Iterable[A], indexesB: Iterable[B])(f : (A, B) => Expression) : Expression = sum( for(a <- indexesA; b <- indexesB) yield f(a, b) )

  def sum[A, B, C](indexesA: Iterable[A], indexesB: Iterable[B], indexesC: Iterable[C])(f : (A, B, C) => Expression) : Expression = {
    sum( for(a <- indexesA; b <- indexesB; c <- indexesC) yield f(a, b, c) )
  }

  def sum[A, B, C, D](indexesA: Iterable[A], indexesB: Iterable[B], indexesC: Iterable[C], indexesD: Iterable[D])(f : (A, B, C, D) => Expression) : Expression = {
    sum( for(a <- indexesA; b <- indexesB; c <- indexesC; d <- indexesD) yield f(a, b, c, d) )
  }

  // algebra implicit conversions

  implicit def Int2Const(value: Int): Const = if(value == 0) Zero else Const(value.toDouble)

  implicit def Double2Const(value: Double): Const = if(value == 0.0) Zero else Const(value)
}
