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
 * The mathematical programming library for Scala.
 *     
 */

package optimus.optimization

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.MPBinaryVar

/**
  * N-Queens puzzle: Place n chess queens on an n×n chessboard so that no two
  * queens threaten each other. Thus, a solution requires that no two queens
  * share the same row, column, or diagonal.
  */
trait Queens extends AnyFunSpec with Matchers {

  def solver: SolverLib

  describe("8 Queens Problem") {

    implicit val queensProblem: MPModel = MPModel(solver)

    val n = 8
    val lines = 0 until n
    val columns = 0 until n

    val x = Array.tabulate(n, n)((l, c) => MPBinaryVar(s"x($l,$c)"))

    maximize(sum(lines, columns) { (l, c) => x(l)(c) })

    // At most one queen can be placed in each row
    for (l <- lines) add(sum(columns)(c => x(l)(c)) <:= 1)

    // At most one queen can be placed in each column
    for (c <- columns) add(sum(lines)(l => x(l)(c)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 1 until n) add(sum(0 to i)(j => x(i - j)(j)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(i until n)(j => x(j)(n - 1 - j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 0 until n) add(sum(0 until n - i)(j => x(j)(j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(0 until n - i)(j => x(j + i)(j)) <:= 1)

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

    val x = Array.tabulate(n, n)((l, c) => MPBinaryVar(s"x($l,$c)"))

    maximize(sum(lines, columns) { (l, c) => x(l)(c) })

    // At most one queen can be placed in each row
    for (l <- lines) add(sum(columns)(c => x(l)(c)) <:= 1)

    // At most one queen can be placed in each column
    for (c <- columns) add(sum(lines)(l => x(l)(c)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 1 until n) add(sum(0 to i)(j => x(i - j)(j)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(i until n)(j => x(j)(n - 1 - j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 0 until n) add(sum(0 until n - i)(j => x(j)(j + i)) <:= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(0 until n - i)(j => x(j + i)(j)) <:= 1)

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
