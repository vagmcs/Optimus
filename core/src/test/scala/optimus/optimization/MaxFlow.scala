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

import optimus.optimization.SolverLib.SolverLib
import org.scalatest.{FunSpec, Matchers}
import optimus.algebra.AlgebraOps._

/**
  * The Maximum Flow Problem in a network G = (V, E), where V is a set of nodes,
  * E within V x V is a set of arcs, is to maximize the flow from one given
  * node s (source) to another given node t (sink) subject to conservation of
  * flow constraints at each node and flow capacities on each arc.
  */
trait MaxFlow extends FunSpec with Matchers {

  def solver: SolverLib

  describe("Max flow problem") {

    implicit val maxFlowProblem: LQProblem = LQProblem(solver)

    val lines = 0 to 7
    val columns = 0 to 8
    val capacities = Array(
      Array(0, 12, 0, 23, 0, 0, 0, 0, 0),
      Array(0, 0, 10, 9, 0, 0, 0, 0, 0),
      Array(0, 0, 0, 0, 12, 0, 0, 18, 0),
      Array(0, 0, 0, 0, 26, 0, 0, 0, 0),
      Array(0, 11, 0, 0, 0, 25, 4, 0, 0),
      Array(0, 0, 0, 0, 0, 0, 7, 8, 0),
      Array(0, 0, 0, 0, 0, 0, 0, 0, 15),
      Array(0, 0, 0, 0, 0, 63, 0, 0, 20)
    )

    val x = Array.tabulate(lines.size, columns.size)(
      (l, c) => MPFloatVar(s"x($l,$c)", 0, capacities(l)(c))
    )

    for (l <- 1 until lines.size)
      add(sum(columns)(c => x(l)(c)) - sum(lines)(c => x(c)(l)) := 0)

    maximize(sum(lines)(l => x(l)(columns.size - 1)))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it(s"$solver objective value should be 29") {
      objectiveValue shouldBe 29
    }

    it("all variables should have a value") {
      for (l <- lines; c <- columns)
        x(l)(c).value.isDefined shouldBe true
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    for (l <- lines) {
      for (c <- columns)
        print(x(l)(c).value.get + "  ")
      println
    }

    release()
  }
}
