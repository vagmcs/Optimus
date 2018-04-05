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

package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra.AlgebraOps._
import optimus.optimization.SolverLib.SolverLib

/**
  * A number of 12 products can be produced. Each of them has a set of features,
  * such as volume, weight, etc. There is a capacity constraint on the total amount
  * that can be produced from each feature; for instance, an upper limit of the
  * total weight of the produced products. Moreover, each product generates a profit
  * per unit produced. The objective is to maximize the total profit, while
  * satisfying these capacity constraints.
  */
trait ProductionPlanning extends FunSpec with Matchers {

  def solver: SolverLib

  describe("Production Planning Problem") {

    implicit val ppProblem: LQProblem = LQProblem(solver)

    // dimensions
    val b = Array(18209, 7692, 1333, 924, 26638, 61188, 13360)

    // products
    val c = Array(96, 76, 56, 11, 86, 10, 66, 86, 83, 12, 9, 81)

    val dimensions = b.indices
    val products = c.indices

    val w = Array(
      Array(19, 1, 10, 1, 1, 14, 152, 11, 1, 1, 1, 1),
      Array(0, 4, 53, 0, 0, 80, 0, 4, 5, 0, 0, 0),
      Array(4, 660, 3, 0, 30, 0, 3, 0, 4, 90, 0, 0),
      Array(7, 0, 18, 6, 770, 330, 7, 0, 0, 6, 0, 0),
      Array(0, 20, 0, 4, 52, 3, 0, 0, 0, 5, 4, 0),
      Array(0, 0, 40, 70, 4, 63, 0, 0, 60, 0, 4, 0),
      Array(0, 32, 0, 0, 0, 5, 0, 3, 0, 660, 0, 9)
    )

    val x = products.map(p => MPFloatVar(s"x$p", 0, 10000))

    maximize(sum(products) { p => x(p) * c(p) })

    for (d <- dimensions)
      add(sum(products)(p => x(p) * w(d)(p)) <:= b(d))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it(s"$solver objective value should be 261972 +- 0.5") {
      objectiveValue shouldBe 261972.0 +- 5e-1
    }

    it("all variables should have a value") {
      x.foreach(_.value.isDefined shouldBe true)
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }
}
