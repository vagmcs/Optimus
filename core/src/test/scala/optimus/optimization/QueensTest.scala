package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra._
import optimus.optimization.SolverLib.SolverLib

/**
  * N-Queens puzzle using MIP programming.
  */
trait QueensTest extends FunSpec with Matchers {

  def solver: SolverLib

  describe(s"QueensTest using ${solver.toString}") {
    val n = 8
    val Lines = 0 until n
    val Columns = 0 until n

    implicit val queensProblem = MIProblem(solver)

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

    status should equal(ProblemStatus.OPTIMAL)
    objectiveValue shouldBe 8.0 +- 0.00001

    release()

    checkConstraints() shouldBe true
    println()
  }
}
