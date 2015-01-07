package optimus.lqprog

import org.scalatest.{FunSpec, Matchers}

/**
 * Specification for LPSolve.
 *
 * @author Vagelis Michelioudakis
 * @author Christos Vlassopoulos
 */
final class LPSolveSpecTest extends FunSpec with Matchers {

  describe("Linear programming") {

    describe("Test I") {
      implicit val problem = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      maximize(-2 * x + 5 * y)
      add(y >= -x + 200)
      start()

      x.value should equal(Some(100))
      y.value should equal(Some(170))
      objectiveValue should equal(650)
      checkConstraints() should be(true)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test II") {
      implicit val problem = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)
      add(y >= -x + 200)
      start()

      x.value should equal(Some(200))
      y.value should equal(Some(80))
      objectiveValue should equal(0)
      checkConstraints() should be(true)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test III") {
      implicit val lp = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x")
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)
      add(y >= -x + 200)
      start()

      // Solution is infeasible but some solvers consider it dual infeasible
      status should (equal(ProblemStatus.UNBOUNDED) or equal(ProblemStatus.INFEASIBLE))

      release()
    }

    describe("Test IV") {
      implicit val lp = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)

      val z = MPFloatVar(lp, "z", 80, 170)

      add(z >= 170)
      add(y >= -x + 200)
      start()

      x.value should equal(Some(200))
      y.value should equal(Some(80))
      z.value should equal(Some(170))
      objectiveValue should equal(0)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test V") {
      implicit val lp = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 0, 10)
      val y = MPFloatVar("y", 0, 10)

      maximize(x + y)
      add(x + y >= 5)
      start()

      x.value should equal(Some(10))
      y.value should equal(Some(10))
      objectiveValue should equal(20)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test VI") {
      implicit val lp = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 0, 10)
      val y = MPFloatVar("y", 0, 10)

      var cons: Vector[MPConstraint] = Vector()

      maximize(x + y)

      cons = cons :+ add(x + y >= 5)
      cons = cons :+ add(x + 2 * y <= 25)
      cons = cons :+ add(x + 2 * y <= 30)
      cons = cons :+ add(x + y >= 17.5)
      cons = cons :+ add(x := 10.0)

      start()

      x.value.get should be(10.0 +- 1e-6)
      y.value.get should be(7.5 +- 1e-6)

      cons(0).isTight() should be(false)
      cons(1).isTight() should be(true)
      cons(2).isTight() should be(false)
      cons(3).isTight() should be(true)
      cons(4).isTight() should be(true)

      cons(0).slack should be(12.5 +- 1e-6)
      cons(1).slack should be(0.0 +- 1e-6)
      cons(2).slack should be(5.0 +- 1e-6)
      cons(3).slack should be(0.0 +- 1e-6)
      cons(4).slack should be(0.0 +- 1e-6)

      cons.foreach(c => c.check() should be(true))

      objectiveValue should be(17.5 +- 1e-6)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test VII") {
      implicit val lp = new LQProblem(SolverLib.lp_solve)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      var cons: Vector[MPConstraint] = Vector()

      maximize(2*x + 4*y + 3*z)

      cons = cons :+ add(3*x + 4*y + 2*z <= 60)
      cons = cons :+ add(2*x + y + 2*z <= 40)
      cons = cons :+ add(x + 3*y + 2*z <= 80)
      cons = cons :+ add(x >= -80)
      cons = cons :+ add(y >= -50)
      cons = cons :+ add(z >= -0.005)

      start()

      x.value.get should be(0.0 +- 1e-6)
      y.value.get should be(6.666666666666667 +- 1e-6)
      z.value.get should be(16.666666666666667 +- 1e-6)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(true)
      cons(2).isTight() should be(false)
      cons(3).isTight() should be(false)
      cons(4).isTight() should be(false)
      cons(5).isTight() should be(false)

      cons(0).slack should be(0.0 +- 1e-6)
      cons(1).slack should be(0.0 +- 1e-6)
      cons(2).slack should be(26.666666666666667 +- 1e-6)
      cons(3).slack should be(80.0 +- 1e-6)
      cons(4).slack should be(56.666666666666667 +- 1e-6)
      cons(5).slack should be(16.671666666666667 +- 1e-6)

      cons.foreach(c => c.check() should be(true))

      objectiveValue should be(76.666666666666667 +- 1e-6)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }
  }

  println()

}
