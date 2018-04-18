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

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{SolutionStatus, SolverLib}
import optimus.optimization.model.MPIntVar

/**
  * N-Queens puzzle: Place n chess queens on an n×n chessboard so that no two
  * queens threaten each other. Thus, a solution requires that no two queens
  * share the same row, column, or diagonal.
  */
trait Queens extends FunSpec with Matchers {

  def solver: SolverLib

  describe("8 Queens Problem") {

    implicit val queensProblem: MPModel = MPModel(solver)

    val n = 8
    val lines = 0 until n
    val columns = 0 until n

    val x = Array.tabulate(n, n)((l, c) => MPIntVar(s"x($l,$c)", 0 to 1))

    maximize(sum(lines, columns) { (l, c) => x(l)(c) })

    // At most one queen can be placed in each row
    for (l <- lines) add(sum(columns)(c => x(l)(c)) <:= 1)

    // At most one queen can be placed in each column
    for (c <- columns) add(sum(lines)(l => x(l)(c)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 1 until n) add(sum(0 to i)((j) => x(i - j)(j)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(i until n)((j) => x(j)(n - 1 - j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 0 until n) add(sum(0 until n - i)((j) => x(j)(j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(0 until n - i)((j) => x(j + i)(j)) <:= 1)

    start()

    it(s"$solver solution status should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 8.0") {
      objectiveValue shouldBe 8.0 +- 1e-2
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution as a board
    for (i <- 0 until n) {
      for (j <- 0 until n)
        if (x(i)(j).value.get >= .9) print(" Q ") else print(" . ")
      println
    }

    release()
  }

  describe("15 Queens Problem") {

    implicit val queensProblem: MPModel = MPModel(solver)

    val n = 15
    val lines = 0 until n
    val columns = 0 until n

    val x = Array.tabulate(n, n)((l, c) => MPIntVar(s"x($l,$c)", 0 to 1))

    maximize(sum(lines, columns) { (l, c) => x(l)(c) })

    // At most one queen can be placed in each row
    for (l <- lines) add(sum(columns)(c => x(l)(c)) <:= 1)

    // At most one queen can be placed in each column
    for (c <- columns) add(sum(lines)(l => x(l)(c)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 1 until n) add(sum(0 to i)((j) => x(i - j)(j)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(i until n)((j) => x(j)(n - 1 - j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 0 until n) add(sum(0 until n - i)((j) => x(j)(j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(0 until n - i)((j) => x(j + i)(j)) <:= 1)

    start()

    it(s"$solver solution status should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 15.0") {
      objectiveValue shouldBe 15.0 +- 1e-2
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution as a board
    for (i <- 0 until n) {
      for (j <- 0 until n)
        if (x(i)(j).value.get >= .9) print(" Q ") else print(" . ")
      println
    }

    release()
  }
}
