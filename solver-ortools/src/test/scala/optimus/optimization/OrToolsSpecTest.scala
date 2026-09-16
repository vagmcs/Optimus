package optimus.optimization

import optimus.algebra._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.{ MPBinaryVar, MPFloatVar, MPIntVar }

/** Specification for the CBC, SCIP, GLOP and CP-SAT solvers accessed through Google OR-Tools. */
final class OrToolsSpecTest extends AnyFunSpec with Matchers {

  def linearProgram(solver: SolverLib): Unit = describe(s"Linear Program ($solver)") {

    implicit val lp: MPModel = MPModel(solver)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo(
      y >:= -x + 200
    )

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("x should be equal to 200") {
      x.value shouldEqual Some(200)
    }

    it("y should be equal to 80") {
      y.value shouldEqual Some(80)
    }

    it("objective value should be equal to 0") {
      objectiveValue shouldEqual 0
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  def unboundedProgram(solver: SolverLib): Unit = describe(s"Unbounded Linear Program ($solver)") {

    implicit val lp: MPModel = MPModel(solver)

    val x = MPFloatVar("x")
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo(
      y >:= -x + 200
    )

    start()

    // Solution is unbounded, but GLOP reports this case as (dual) infeasible instead
    it("solution should be unbounded or infeasible") {
      status should (equal(SolutionStatus.UNBOUNDED) or equal(SolutionStatus.INFEASIBLE))
    }

    release()
  }

  def infeasibleProgram(solver: SolverLib): Unit = describe(s"Infeasible Program ($solver)") {

    implicit val lp: MPModel = MPModel(solver)

    val x = MPFloatVar("x", 0, 10)

    maximize(x)
    add(x >:= 20)

    start()

    it("solution should be infeasible") {
      status shouldBe SolutionStatus.INFEASIBLE
    }

    release()
  }

  def mixedIntegerProgram(solver: SolverLib): Unit = describe(s"Mixed-Integer Program ($solver)") {

    implicit val mip: MPModel = MPModel(solver)

    val x = MPFloatVar("x", 0, 100)
    val y = MPIntVar("y", 0 to 100)

    maximize(8 * x + 12 * y)
    add(10 * x + 20 * y <:= 140)
    add(6 * x + 8 * y <:= 72)

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("x should be equal to 8") {
      x.value.get shouldEqual 8.0 +- 1e-2
    }

    it("y should be equal to 3") {
      y.value.get shouldEqual 3
    }

    it("objective value should be equal to 100") {
      objectiveValue shouldEqual 100.0 +- 1e-2
    }

    release()
  }

  def binaryProgram(solver: SolverLib): Unit = describe(s"Binary Program ($solver)") {

    implicit val mip: MPModel = MPModel(solver)

    val x = Array.tabulate(6)(j => MPBinaryVar(s"x$j"))

    minimize(3 * x(0) + 5 * x(1) + 6 * x(2) + 9 * x(3) + 10 * x(4) + 10 * x(5))

    add(-2 * x(0) + 6 * x(1) - 3 * x(2) + 4 * x(3) + x(4) - 2 * x(5) >:= 2)
    add(-5 * x(0) - 3 * x(1) + x(2) + 3 * x(3) - 2 * x(4) + x(5) >:= -2)
    add(5 * x(0) - x(1) + 4 * x(2) - 2 * x(3) + 2 * x(4) - x(5) >:= 3)

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("objective value should be equal to 11") {
      objectiveValue shouldEqual 11
    }

    release()
  }

  // CBC and SCIP are general purpose LP/MIP solvers, unbounded domains included.
  Seq(SolverLib.Cbc, SolverLib.Scip).foreach { solver =>
    linearProgram(solver)
    unboundedProgram(solver)
    infeasibleProgram(solver)
    mixedIntegerProgram(solver)
    binaryProgram(solver)
  }

  // GLOP is a pure LP solver, integrality is ignored and the linear relaxation is solved.
  linearProgram(SolverLib.Glop)
  unboundedProgram(SolverLib.Glop)
  infeasibleProgram(SolverLib.Glop)

  // CP-SAT (the "SAT" backend) requires all variables to have finite bounds, so it is only
  // exercised with bounded problems here.
  mixedIntegerProgram(SolverLib.Sat)
  binaryProgram(SolverLib.Sat)
  infeasibleProgram(SolverLib.Sat)
}
