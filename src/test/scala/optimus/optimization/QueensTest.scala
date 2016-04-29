package optimus.optimization

import org.scalatest.{Matchers, FunSpec}
import optimus.algebra._

/**
  * N-Queens puzzle: Place n chess queens on an n×n chessboard so that no two
  * queens threaten each other. Thus, a solution requires that no two queens
  * share the same row, column, or diagonal.
  */
final class QueensTest extends FunSpec with Matchers {

  val n = 8
  val Lines = 0 until n
  val Columns = 0 until n

  for (lib <- solvers) {

    implicit val queensProblem = MIProblem(lib)

    val x = Array.tabulate(n, n)((l, c) => MPIntVar("x" +(l, c), 0 to 1))

    maximize(sum(Lines, Columns) { (l, c) => x(l)(c) })

    // At most one queen can be placed in each row
    for (l <- Lines) add(sum(Columns)(c => x(l)(c)) <= 1)

    // At most one queen can be placed in each column
    for (c <- Columns) add(sum(Lines)(l => x(l)(c)) <= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 1 until n) add(sum(0 to i)((j) => x(i - j)(j)) <= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(i until n)((j) => x(j)(n - 1 - j + i)) <= 1)

    // At most one queen can be placed in each /-diagonal upper half
    for (i <- 0 until n) add(sum(0 until n - i)((j) => x(j)(j + i)) <= 1)

    // At most one queen can be placed in each /-diagonal lower half
    for (i <- 1 until n) add(sum(0 until n - i)((j) => x(j + i)(j)) <= 1)

    start()

    it(s"$lib solution status should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it(s"$lib objective value should be 8.0 +- 0.00001") {
      objectiveValue shouldBe 8.0 +- 0.00001
    }

    it(s"$lib constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }
}
