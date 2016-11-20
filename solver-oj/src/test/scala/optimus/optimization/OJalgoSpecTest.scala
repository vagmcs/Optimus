package optimus.optimization

import org.scalatest.{FunSpec, Matchers}

/**
  * Specification for oJalgo.
  */
final class OJalgoSpecTest extends FunSpec with Matchers {

  describe("Linear programming") {

    describe("Test I") {
      implicit val problem = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      maximize(-2 * x + 5 * y)
      add(y >:= -x + 200)
      start(PreSolve.CONSERVATIVE)

      x.value should equal(Some(100))
      y.value should equal(Some(170))
      objectiveValue should equal(650)
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test II") {
      implicit val problem = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)
      add(y >:= -x + 200)
      start()

      x.value should equal(Some(200))
      y.value should equal(Some(80))
      objectiveValue should equal(0)
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test III") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x")
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)
      add(y >:= -x + 200)
      start()

      // Solution is infeasible but some solvers consider it dual infeasible
      status should (equal(ProblemStatus.UNBOUNDED) or equal(ProblemStatus.INFEASIBLE))

      release()
    }

    describe("Test IV") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)

      val z = MPFloatVar(lp, "z", 80, 170)

      add(z >:= 170)
      add(y >:= -x + 200)
      start()

      x.value should equal(Some(200))
      y.value should equal(Some(80))
      z.value should equal(Some(170))
      objectiveValue should equal(0)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test V") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 0, 10)
      val y = MPFloatVar("y", 0, 10)

      maximize(x + y)
      add(x + y >:= 5)
      start()

      x.value should equal(Some(10))
      y.value should equal(Some(10))
      objectiveValue should equal(20)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test VI") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 0, 10)
      val y = MPFloatVar("y", 0, 10)

      var cons: Vector[MPConstraint] = Vector()

      maximize(x + y)

      cons = cons :+ add(x + y >:= 5)
      cons = cons :+ add(x + 2 * y <:= 25)
      cons = cons :+ add(x + 2 * y <:= 30)
      cons = cons :+ add(x + y >:= 17.5)
      cons = cons :+ add(x := 10.0)

      start()

      x.value.get shouldBe 10.0 +- 1e-6
      y.value.get shouldBe 7.5 +- 1e-6

      cons.head.isTight() shouldBe false
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe true
      cons(4).isTight() shouldBe true

      cons.head.slack.get shouldBe 12.5 +- 1e-6
      cons(1).slack.get shouldBe 0.0 +- 1e-6
      cons(2).slack.get shouldBe 5.0 +- 1e-6
      cons(3).slack.get shouldBe 0.0 +- 1e-6
      cons(4).slack.get shouldBe 0.0 +- 1e-6

      cons.foreach(c => c.check() shouldBe true)

      objectiveValue shouldBe 17.5 +- 1e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test VII") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      var cons: Vector[MPConstraint] = Vector()

      maximize(2*x + 4*y + 3*z)

      cons = cons :+ add(3*x + 4*y + 2*z <:= 60)
      cons = cons :+ add(2*x + y + 2*z <:= 40)
      cons = cons :+ add(x + 3*y + 2*z <:= 80)
      cons = cons :+ add(x >:= -80)
      cons = cons :+ add(y >:= -50)
      cons = cons :+ add(z >:= -0.005)

      start()

      x.value.get shouldBe 0.0 +- 1e-6
      y.value.get shouldBe 6.666666666666667 +- 1e-6
      z.value.get shouldBe 16.666666666666667 +- 1e-6

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe false
      cons(4).isTight() shouldBe false
      cons(5).isTight() shouldBe false

      cons.head.slack.get shouldBe 0.0 +- 1e-6
      cons(1).slack.get shouldBe 0.0 +- 1e-6
      cons(2).slack.get shouldBe 26.666666666666667 +- 1e-6
      cons(3).slack.get shouldBe 80.0 +- 1e-6
      cons(4).slack.get shouldBe 56.666666666666667 +- 1e-6
      cons(5).slack.get shouldBe 16.671666666666667 +- 1e-6

      cons.foreach(c => c.check() shouldBe true)

      objectiveValue shouldBe 76.666666666666667 +- 1e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test VIII") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val w = MPFloatVar("w", 0, Double.PositiveInfinity)
      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      var cons: Vector[MPConstraint] = Vector()

      maximize(3*w - 8*w + 10*w + 0.001*x - (-0.999*x) - 0.3*10*(-y) - 4*0.0006*0*(w-x-z) + 2*z - 2*z + 4*z)

      cons = cons :+ add(w + x + y + z <:= 40)
      cons = cons :+ add(2*w + x - y - z >:= 10)

      start()

      w.value.get should equal (4.0e+1 +- 1.0e-6)
      x.value.get should equal (0.0 +- 1.0e-6)
      y.value.get should equal (0.0 +- 1.0e-6)
      z.value.get should equal (0.0 +- 1.0e-6)
      objectiveValue shouldBe 2.0e+2 +- 1.0e-6

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe false

      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() shouldBe true

      //TODO: Constraint of the form y >:= w doesn't work. Seen as boolean.
      cons = cons :+ add(y - w >:= 0)
      cons = cons :+ add(x >:= 15)
      start()

      w.value.get should equal (6.666666666666666 +- 1.0e-6)   // 12.5
      x.value.get should equal (1.5e+1 +- 1.0e-6)
      y.value.get should equal (6.666666666666666 +- 1.0e-6)   // 12.5
      z.value.get should equal (11.666666666666666 +- 1.0e-6)  //  0.0
      objectiveValue shouldBe 1.15e+2 +- 1.0e-6

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe true   // Gurobi says false
      cons(2).isTight() shouldBe true
      cons(3).isTight() shouldBe true

      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() shouldBe true

      // Constraint: w - 2x + 4y + 3z >:= 40
      cons = cons :+ add(-(-w) - 2*x + 4*y + 3*0.5*2*z >:= 40 - 3 + 2.7 + 0.3)
      start()

      w.value.get should equal (6.66666667 +- 1.0e-6)
      x.value.get should equal (1.5e+1 +- 1.0e-6)
      y.value.get should equal (8.33333333 +- 1.0e-6)
      z.value.get should equal (1.0e+1 +- 1.0e-6)
      objectiveValue shouldBe 1.1333333333e+2 +- 1.0e-6
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() shouldBe true

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe true
      cons(4).isTight() shouldBe true

      release()
    }
  }

  describe("Quadratic programming") {

    describe("Test III") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(-8*x - 16*y + x*x + 4*y*y)

      cons = cons :+ add(x + y <:= 5)
      cons = cons :+ add(x <:= 3)
      start()

      x.value.get should equal (3.0 +- 1.0e-6)
      y.value.get should equal (2.0 +- 1.0e-6)  // Here gurobi requires +- 1.0e-4

      cons.head.isTight() shouldBe true  // Here gurobi fails
      cons.head.isTight(10e-4) shouldBe true
      cons(1).isTight() shouldBe true

      cons.head.slack.get shouldBe 0.0 +- 1.0e-6
      cons(1).slack.get shouldBe 0.0 +- 1.0e-6

      cons.foreach(c => c.check() shouldBe true)

      objectiveValue shouldBe -31.0 +- 1.0e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test IV") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(2*x*x + y*y + x*y + x + y)
      cons = cons :+ add(x + y := 1)
      cons = cons :+ add(x >:= -3)
      cons = cons :+ add(y >:= -1.0e-4)
      start()

      x.value.get should equal (2.5e-1 +- 1.0e-6)
      y.value.get should equal (7.5e-1 +- 1.0e-6)

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe false
      cons(2).isTight() shouldBe false

      cons.head.slack.get shouldBe 0.0 +- 1.0e-6
      cons(1).slack.get shouldBe 3.25 +- 1.0e-6
      cons(2).slack.get shouldBe 7.501e-1 +- 1.0e-6

      cons.foreach(c => c.check() shouldBe true)

      objectiveValue shouldBe 1.875 +- 1.0e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test V") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(x*x + x*x + y*y - y*y + y*y + 7*x*y - 6*y*x + x*x - x*x + x - 99.9e-9*y + 1.0000000999*y)
      cons = cons :+ add(x + y := 1)
      cons = cons :+ add(x >:= -3)
      cons = cons :+ add(y >:= -1.0e-4)
      start()

      x.value.get should equal (2.5e-1 +- 1.0e-6)
      y.value.get should equal (7.5e-1 +- 1.0e-6)

      cons.head.isTight() shouldBe true
      cons(1).isTight() shouldBe false
      cons(2).isTight() shouldBe false

      cons.head.slack.get shouldBe 0.0 +- 1.0e-6
      cons(1).slack.get shouldBe 3.25 +- 1.0e-6
      cons(2).slack.get shouldBe 7.501e-1 +- 1.0e-6

      cons.foreach(c => c.check() shouldBe true)

      objectiveValue shouldBe 1.875 +- 1.0e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    /**
     * Almost identical to III
     */
    describe("Test VI") {
      implicit val lp = LQProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(-8*x - 16*y + x*x + 4*y*y)
      subjectTo( x + y <:= 5,
        x <:= 3,
        x >:= 0,
        y >:= 0 )
      start()

      x.value.get should equal (2.9999999998374056 +- 0.0001)
      y.value.get should equal (1.999958833749785 +- 0.0001)
      objectiveValue shouldBe -3.10000000e+01 +- 0.0001
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() shouldBe true

      release()
    }
  }

  describe("Mixed-Integer Programming") {

    describe("Test I") {
      implicit val mip = MIProblem(SolverLib.ojalgo)

      val x0 = MPFloatVar("x0", 0, 40)
      val x1 = MPIntVar("x1", 0 to 1000)
      val x2 = MPIntVar("x2", 0 until 18)
      val x3 = MPFloatVar("x3", 2, 3)

      maximize(x0 + 2*x1 + 3*x2 + x3)
      subjectTo(
        -1*x0 + x1 + x2 + 10*x3 <:= 20,
        x0 - 3.0*x1 + x2 <:= 30,
        x1 - 3.5*x3 := 0
      )

      start()

      x0.value.get should equal (39.999999 +- 1.0e-6)
      x1.value.get should equal (10.0 )
      x2.value.get should equal (17.0)
      x3.value.get should equal (2.8571428 +- 1.0e-6)
      objectiveValue shouldBe 113.857142 +- 1.0e-6
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test II") {

      implicit val mip = MIProblem(SolverLib.ojalgo)

      val x = MPFloatVar("x", 0, 100)
      val y = MPIntVar("y", 0 to 100)

      maximize(8 * x + 12 * y)
      add(10 * x + 20 * y <:= 140)
      add(6 * x + 8 * y <:= 72)
      start()

      status should equal(ProblemStatus.OPTIMAL)
      x.value.get shouldBe 8.0 +- 0.00001
      y.value should equal(Some(3))
      release()
    }

    describe("Test III") {

      implicit val lp = MIProblem(SolverLib.ojalgo)

      val x = Array.tabulate(6)(j => MPIntVar(s"x$j", 0 to 1))
      val z = 3 * x(0) + 5 * x(1) + 6 * x(2) + 9 * x(3) + 10 * x(4) + 10 * x(5)
      minimize(z)
      add(-2 * x(0) + 6 * x(1) - 3 * x(2) + 4 * x(3) + x(4) - 2 * x(5) >:= 2)
      add(-5 * x(0) - 3 * x(1) + x(2) + 3 * x(3) - 2 * x(4) + x(5) >:= -2)
      add(5 * x(0) - x(1) + 4 * x(2) -2 * x(3) + 2 * x(4) - x(5) >:= 3)

      x.foreach(_.isBinary shouldBe true)

      start()
      status should equal(ProblemStatus.OPTIMAL)
      lp.objectiveValue shouldBe 11.0
      release()
    }
  }

  println()
}
