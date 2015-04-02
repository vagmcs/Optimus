package optimus

import gnu.trove.map.hash.TLongDoubleHashMap
import gnu.trove.procedure.TLongDoubleProcedure

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
 * Helper functions for summation of multiple expressions
 * stored in iterable data structures and encoding/decoding of terms.
 *
 * @author Vagelis Michelioudakis
 * @author Anastasios Skarlatidis
 */
package object algebra {

  /**
   * Cantor pairing function. A process to uniquely encode natural numbers
   * into a single natural number.
   *
   * @param x the first number
   * @param y the second number (default is -1 in case we want to encode only one)
   * @return a unique number for x and y
   */
  def encode(x: Int, y: Int = -1): Long = {
    val xm: Long = 2 * x
    val ym: Long = if(y == -1) 1 else 2 * y
    val w = xm + ym
    (w * (w + 1) / 2) + ym
  }

  /**
   * Cantor inverse pairing function. Uniquely decodes a number into a sequence of
   * natural numbers they produced it.
   *
   * @param z the number to decode
   * @return a pair of numbers or one number if default value was used during encoding
   */
  def decode(z: Long): Vector[Int] = {
    val w = Math.floor( (-1D + Math.sqrt(1D + 8 * z)) / 2D)
    val x = (w * (w + 3) / 2 - z) / 2
    val y = z - w * (w + 1) / 2
    if(y == 1) Vector(x.toInt)
    else Vector(x.toInt, y.toInt / 2)
  }

  // functions over iterable data structures

  def sum(expressions: Iterable[Expression]) : Expression = {

    val temporal = new TLongDoubleHashMap()
    var tConstant = 0.0

    for (expr <- expressions) {
      tConstant += expr.constant
      val iterator = expr.terms.iterator
      while(iterator.hasNext) {
        iterator.advance()
        val coefficient = iterator.value
        temporal.adjustOrPutValue(iterator.key, coefficient, coefficient)
      }
    }
    temporal.retainEntries(new TLongDoubleProcedure { override def execute(l: Long, v: Double): Boolean = v != 0.0 })

    new Expression {
      val constant = tConstant
      val terms = temporal
    }
  }

  // these functions produce mathematical expressions over joint iterable and then summing out the results

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
