package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra.AlgebraOps._
import optimus.optimization.SolverLib.SolverLib

/**
  * N-Queens puzzle: Place n chess queens on an n×n chessboard so that no two
  * queens threaten each other. Thus, a solution requires that no two queens
  * share the same row, column, or diagonal.
  */
trait QueensTest extends FunSpec with Matchers {

  def solver: SolverLib

  val n = 8
  private val lines = 0 until n
  private val columns = 0 until n

  implicit val queensProblem: MIProblem = MIProblem(solver)

  private val x = Array.tabulate(n, n)((l, c) => MPIntVar("x" +(l, c), 0 to 1))

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
    status shouldBe ProblemStatus.OPTIMAL
  }

  it(s"$solver objective value should be 8.0 +- 0.00001") {
    objectiveValue shouldBe 8.0 +- 0.00001
  }

  it(s"$solver constraints should be satisfied") {
    checkConstraints() shouldBe true
  }

  release()
}
