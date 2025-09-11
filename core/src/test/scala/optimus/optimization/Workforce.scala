package optimus.optimization

import optimus.algebra._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.MPBinaryVar

/**
 * Assign workers to shifts while satisfying requirements for that day.
 * Each worker may or may not be available on a particular day and therefore
 * the objective is to minimize the total payment costs.
 */
trait Workforce extends AnyFunSpec with Matchers {

  def solver: SolverLib

  describe("Workforce Problem") {

    implicit val workforceProblem: MPModel = MPModel(solver)

    val shifts = 0 to 13
    val workers = 0 to 6
    val shiftRequirements = Array(3, 2, 4, 1, 5, 2, 4, 2, 2, 3, 4, 5, 3, 5)

    // salary of each person
    val pay = Array(10, 12, 10, 8, 8, 9, 11)

    // maximum number of shifts a worker can be assigned to in the schedule
    val maxNbShift = 7

    val availability = Array(
      Array(0, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1),
      Array(1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0),
      Array(0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1),
      Array(0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1),
      Array(1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1),
      Array(1, 1, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 1),
      Array(1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
    )

    val assigned = Array.tabulate(workers.size, shifts.size)((i, j) => MPBinaryVar(s"x($i,$j)"))

    minimize(sum(workers, shifts)((w, s) => assigned(w)(s) * pay(w).toDouble))

    for (s <- shifts.indices)
      add(sum(workers)(w => assigned(w)(s) * availability(w)(s).toDouble) := shiftRequirements(s).toDouble)

    for (w <- workers) add(sum(shifts)(s => assigned(w)(s)) <:= maxNbShift.toDouble)

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 428") {
      objectiveValue shouldBe 428
    }

    it("all variables should have a value") {
      for (w <- workers; s <- shifts.indices) assigned(w)(s).value.isDefined shouldBe true
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    for (s <- shifts.indices)
      println("Day " + s + " workers: " + workers.filter(w => assigned(w)(s).value.get == 1).mkString(", "))

    release()
  }
}
