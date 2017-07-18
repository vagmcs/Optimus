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

package optimus

import gnu.trove.map.hash.TLongDoubleHashMap
import gnu.trove.procedure.TLongDoubleProcedure
import scala.language.implicitConversions

/**
  * Helper functions for summation of multiple expressions
  * stored in iterable data structures and encoding/decoding of terms.
  */
package object algebra {

  type UniqueId = Long
  type DecodedIds = Vector[Int]

  /**
    * Szudzik pairing function is a process for uniquely encoding a pair of natural
    * numbers into a single natural number. It is used by algebra to encode variable
    * products by ID and produce unique product IDs.
    *
    * @note This variant encodes a variable ID alone.
    *
    * @param x a variable ID
    * @return a unique ID in the space of encodings for a single variable ID
    */
  def encode(x: Long): UniqueId = encode(x, -1)

  /**
    * Szudzik pairing function is a process that uniquely encodes a pair of natural
    * numbers into a single natural number. It is used by algebra to encode variable
    * products by ID and produce unique product IDs.
    *
    * @param x a variable ID
    * @param y another variable ID
    * @return a unique ID in the space of encodings for x and y
    */
  def encode(x: Long, y: Long): UniqueId =
    if (x < y) (y + 1) * (y + 1) + (x + 1) else (x + 1) * (x + 1) + (y + 1)

  /**
    * Szudzik pairing function is a process for uniquely encoding a pair of natural
    * numbers into a single natural number. It is used by algebra to encode variable
    * products by ID and produce unique product IDs.
    *
    * @note This variant encodes a vector of variables. The vector should contain
    *       a single variable or a pair of variables.
    *
    * @param vars a vector of variables
    * @return a unique ID in the space of encodings for the variables
    */
  def encode(vars: Vector[Variable]): UniqueId = {
    if (vars.size == 1) encode(vars.head.index)
    else encode(vars.head.index, vars.last.index)
  }

  /**
    * Szudzik inverse pairing function. Uniquely decodes a natural
    * number encoding into the pair of natural numbers that produced
    * the encoding.
    *
    * @note In case the encoding refers to a single variable the vector
    *       will contain only
    *
    * @param z the number to decode
    * @return a vector holding the pair of numbers
    */
  def decode(z: UniqueId): DecodedIds = {
    val q = math.floor(math.sqrt(z))
    val l = z - q * q
    Vector(l.toInt - 1, q.toInt - 1).filter(_ > -1)
  }

  // Functions over iterable data structures of expressions

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
    temporal.retainEntries(new TLongDoubleProcedure {
      override def execute(l: Long, v: Double): Boolean = v != 0.0 })

    new Expression {
      val constant = tConstant
      val terms = temporal
    }
  }

  // These functions produce mathematical expressions over joint iterable and then summing out the results

  def sum[A](indexes: Iterable[A])(f: A => Expression): Expression = sum(indexes map f)

  def sum[A, B](indexesA: Iterable[A],
                indexesB: Iterable[B])(f: (A, B) => Expression): Expression = {
    sum( for(a <- indexesA; b <- indexesB) yield f(a, b) )
  }

  def sum[A, B, C](indexesA: Iterable[A], indexesB: Iterable[B],
                   indexesC: Iterable[C])(f: (A, B, C) => Expression): Expression = {
    sum( for(a <- indexesA; b <- indexesB; c <- indexesC) yield f(a, b, c) )
  }

  def sum[A, B, C, D](indexesA: Iterable[A], indexesB: Iterable[B],
                      indexesC: Iterable[C], indexesD: Iterable[D])(f: (A, B, C, D) => Expression): Expression = {
    sum( for(a <- indexesA; b <- indexesB; c <- indexesC; d <- indexesD) yield f(a, b, c, d) )
  }

  // Algebra implicit conversions

  implicit def Int2Const(value: Int): Const =
    if(value == 0) Zero else Const(value.toDouble)

  implicit def Double2Const(value: Double): Const =
    if(value == 0.0) Zero else Const(value)
}
