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

import gnu.trove.procedure.TLongDoubleProcedure
import scala.language.implicitConversions

object AlgebraOps {

  implicit def function2TLongDoubleProcedure(f: (Long, Double) => Boolean): TLongDoubleProcedure =
    new TLongDoubleProcedure {
      override def execute(x: UniqueId, y: Double): Boolean = f(x, y)
    }

  // Functions over iterable data structures of expressions

  def sum(expressions: Iterable[Expression]): Expression = {
    val resultedTerms: LongDoubleMap = LongDoubleMap.empty
    var const: Double = 0d

    for (expr <- expressions) {
      const += expr.constant

      val iterator = expr.terms.iterator
      while (iterator.hasNext) {
        iterator.advance()
        val scalar = iterator.value
        resultedTerms.adjustOrPutValue(iterator.key, scalar, scalar)
      }
    }
    resultedTerms.retainEntries((_: Long, v: Double) => v != 0d)

    new Expression {
      override val constant: Double = const
      override val terms: LongDoubleMap = resultedTerms
    }
  }

  // Functions that produce mathematical expressions over joint iterable and then summing out the results

  def sum[A](indexes: Iterable[A])(f: A => Expression): Expression = sum(indexes map f)

  def sum[A, B](
      indexesA: Iterable[A],
      indexesB: Iterable[B])
    (f: (A, B) => Expression): Expression = sum {
    for (a <- indexesA; b <- indexesB) yield f(a, b)
  }

  def sum[A, B, C](
      indexesA: Iterable[A],
      indexesB: Iterable[B],
      indexesC: Iterable[C])
    (f: (A, B, C) => Expression): Expression = sum {
    for (a <- indexesA; b <- indexesB; c <- indexesC) yield f(a, b, c)
  }

  def sum[A, B, C, D](
      indexesA: Iterable[A],
      indexesB: Iterable[B],
      indexesC: Iterable[C],
      indexesD: Iterable[D])
    (f: (A, B, C, D) => Expression): Expression = sum {
    for (a <- indexesA; b <- indexesB; c <- indexesC; d <- indexesD) yield f(a, b, c, d)
  }
}
