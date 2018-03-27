package optimus.optimization

import org.scalatest.{FunSpec, Matchers}

/**
  * Specification for Gurobi solver.
  */
final class GurobiSpecTest extends FunSpec with Matchers {

  // Constant objective function tests

  describe("Constant Program (1)") {

    implicit val cp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    maximize(-5)

    add(x >:= 5)
    add(y <:= 100)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 200") {
      x.value shouldEqual Some(200)
    }

    it("y should be equal to 80") {
      y.value shouldEqual Some(80)
    }

    it("objective value should be equal to -5") {
      objectiveValue shouldEqual -5
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  describe("Constant Program (2)") {

    implicit val cp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-5)

    add(x >:= 150)
    add(y <:= 100)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 200") {
      x.value shouldEqual Some(200)
    }

    it("y should be equal to 170") {
      y.value shouldEqual Some(80)
    }

    it("objective value should be equal to -5") {
      objectiveValue shouldEqual -5
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  // Linear objective function tests

  describe("Linear Program (1)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    maximize(-2 * x + 5 * y)
    subjectTo (
      y >:= -x + 200
    )

    start(PreSolve.CONSERVATIVE)

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 100") {
      x.value shouldEqual Some(100)
    }

    it("y should be equal to 170") {
      y.value shouldEqual Some(170)
    }

    it("objective value should be equal to 650") {
      objectiveValue shouldEqual 650
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  describe("Linear Program (2)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo (
      y >:= -x + 200
    )

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
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

  describe("Linear Program (3)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x")
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo (
      y >:= -x + 200
    )

    start()

    // Solution is infeasible but some solvers consider it dual infeasible
    it("solution should be infeasible") {
      status should (equal(ProblemStatus.UNBOUNDED) or equal(ProblemStatus.INFEASIBLE))
    }

    it("x should be None") {
      x.value shouldBe None
    }

    it("y should be None") {
      y.value shouldBe None
    }

    it("constraints should be unsatisfied") {
      checkConstraints() shouldBe false
    }

    release()
  }

  describe("Linear Program (4)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)

    val z = MPFloatVar(lp, "z", 80, 170)

    subjectTo (
      z >:= 170,
      y >:= -x + 200
    )

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 200") {
      x.value shouldEqual Some(200)
    }

    it("y should be equal to 80") {
      y.value shouldEqual Some(80)
    }

    it("z should be equal to 170") {
      z.value shouldEqual Some(170)
    }

    it("objective value should be equal to 0") {
      objectiveValue shouldEqual 0
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  describe("Linear Program (5)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, 10)
    val y = MPFloatVar("y", 0, 10)

    maximize(x + y)
    subjectTo (
      x + y >:= 5
    )

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 10") {
      x.value shouldEqual Some(10)
    }

    it("y should be equal to 10") {
      y.value shouldEqual Some(10)
    }

    it("objective value should be equal to 20") {
      objectiveValue shouldEqual 20
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  describe("Linear Program (6)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, 10)
    val y = MPFloatVar("y", 0, 10)

    var cons = Vector.empty[MPConstraint]

    maximize(x + y)

    cons = cons :+ add(x + y >:= 5)
    cons = cons :+ add(x + 2 * y <:= 25)
    cons = cons :+ add(x + 2 * y <:= 30)
    cons = cons :+ add(x + y >:= 17.5)
    cons = cons :+ add(x := 10.0)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 10.0") {
      x.value.get shouldEqual 10.0 +- 1e-2
    }

    it("y should be equal to 7.5") {
      y.value.get shouldEqual 7.5 +- 1e-2
    }

    it("objective value should be equal to 17.5") {
      objectiveValue shouldEqual 17.5 +- 1e-2
    }

    it("check constraints") {
      cons(0).isTight() shouldBe false
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe true
      cons(4).isTight() shouldBe true

      cons(0).slack.get shouldBe 12.5 +- 1e-2
      cons(1).slack.get shouldBe 0.0 +- 1e-2
      cons(2).slack.get shouldBe 5.0 +- 1e-2
      cons(3).slack.get shouldBe 0.0 +- 1e-2
      cons(4).slack.get shouldBe 0.0 +- 1e-2

      cons.foreach(_.check() shouldBe true)
    }

    release()
  }

  describe("Linear Program (7)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)
    val z = MPFloatVar("z", 0, Double.PositiveInfinity)

    var cons = Vector.empty[MPConstraint]

    maximize(2*x + 4*y + 3*z)

    cons = cons :+ add(3*x + 4*y + 2*z <:= 60)
    cons = cons :+ add(2*x + y + 2*z <:= 40)
    cons = cons :+ add(x + 3*y + 2*z <:= 80)
    cons = cons :+ add(x >:= -80)
    cons = cons :+ add(y >:= -50)
    cons = cons :+ add(z >:= -0.005)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 0.0") {
      x.value.get shouldEqual 0.0 +- 1e-2
    }

    it("y should be equal to 6.67") {
      y.value.get shouldEqual 6.67 +- 1e-2
    }

    it("z should be equal to 16.67") {
      z.value.get shouldEqual 16.67 +- 1e-2
    }

    it("objective value should be equal to 76.67") {
      objectiveValue shouldEqual 76.67 +- 1e-2
    }

    it("check constraints") {
      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe false
      cons(4).isTight() shouldBe false
      cons(5).isTight() shouldBe false

      cons(0).slack.get shouldBe 0.0 +- 1e-2
      cons(1).slack.get shouldBe 0.0 +- 1e-2
      cons(2).slack.get shouldBe 26.67 +- 1e-2
      cons(3).slack.get shouldBe 80.0 +- 1e-2
      cons(4).slack.get shouldBe 56.67 +- 1e-2
      cons(5).slack.get shouldBe 16.67 +- 1e-2

      cons.foreach(_.check() shouldBe true)
    }

    release()
  }

  describe("Linear Program (8)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val w = MPFloatVar("w", 0, Double.PositiveInfinity)
    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)
    val z = MPFloatVar("z", 0, Double.PositiveInfinity)

    var cons: Vector[MPConstraint] = Vector()

    maximize(3*w - 8*w + 10*w + 0.001*x - (-0.999*x) - 0.3*10*(-y) - 4*0.0006*0*(w - x - z) + 2*z - 2*z + 4*z)

    cons = cons :+ add(w + x + y + z <:= 40)
    cons = cons :+ add(2*w + x - y - z >:= 10)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x, y and z should be equal to 0.0") {
      x.value.get shouldEqual 0.0 +- 1e-2
      y.value.get shouldEqual 0.0 +- 1e-2
      z.value.get shouldEqual 0.0 +- 1e-2
    }

    it("w should be equal to 40.0") {
      w.value.get shouldEqual 40.0 +- 1e-2
    }

    it("objective value should be equal to 200.0") {
      objectiveValue shouldEqual 200.0 +- 1e-2
    }

    it("constraints should be satisfied") {
      cons.head.isTight() shouldBe true
      cons.last.isTight() shouldBe false
      checkConstraints() shouldBe true
    }

    it("Add a couple of constraints and re-optimize") {

      cons = cons :+ add(y >:= w)
      cons = cons :+ add(x >:= 15)

      start()

      w.value.get should equal(12.5 +- 1e-2)
      x.value.get should equal(15.0 +- 1e-2)
      y.value.get should equal(12.5 +- 1e-2)
      z.value.get should equal(0.0 +- 1e-2)
      objectiveValue shouldBe (115.0 +- 1e-2)

      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe false
      cons(2).isTight() shouldBe true
      cons(3).isTight() shouldBe true

      status shouldEqual ProblemStatus.OPTIMAL
      checkConstraints() shouldBe true

      // Constraint: w - 2x + 4y + 3z >:= 40
      cons :+= add(-(-w) - 2 * x + 4 * y + 3 * 0.5 * 2 * z >:= 40 - 3 + 2.7 + 0.3)
      start()

      w.value.get should equal(6.67 +- 1e-2)
      x.value.get should equal(15.0 +- 1e-2)
      y.value.get should equal(8.33 +- 1e-2)
      z.value.get should equal(10.0 +- 1e-2)
      objectiveValue shouldBe (113.33 +- 1e-2)
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() shouldBe true

      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe true
      cons(4).isTight() shouldBe true

      release()
    }

  }

  describe("Linear Program (9)") {

    implicit val lp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, 10)

    maximize(x + 1)
    subjectTo (
      x <:= 1
    )

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x should be equal to 1.0") {
      x.value shouldEqual Some(1.0)
    }

    it("objective value should be equal to 2.0") {
      objectiveValue shouldEqual 2.0
    }

    release()
  }

  // Mixed-integer objective function tests

  describe("Mixed-Integer Program (1)") {

    implicit val mip: MIProblem = MIProblem(SolverLib.gurobi)

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

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("x0 should be equal to 39.9") {
      x0.value.get shouldEqual 39.99 +- 1e-2
    }

    it("x1 should be equal to 10") {
      x1.value.get shouldEqual 10
    }

    it("x2 should be equal to 17") {
      x2.value.get shouldEqual 17
    }

    it("x3 should be equal to 2.85") {
      x3.value.get shouldEqual 2.85 +- 1e-2
    }

    it("objective value should be equal to 113.85") {
      objectiveValue shouldEqual 113.85 +- 1e-2
    }

    release()
  }

  describe("Mixed-Integer Program (2)") {

    implicit val mip: MIProblem = MIProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, 100)
    val y = MPIntVar("y", 0 to 100)

    maximize(8 * x + 12 * y)
    add(10 * x + 20 * y <:= 140)
    add(6 * x + 8 * y <:= 72)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
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

  describe("Mixed-Integer Program (3)") {

    implicit val mip: MIProblem = MIProblem(SolverLib.gurobi)

    val x = Array.tabulate(6)(j => MPIntVar(s"x$j", 0 to 1))

    val z = 3 * x(0) + 5 * x(1) + 6 * x(2) + 9 * x(3) + 10 * x(4) + 10 * x(5)

    minimize(z)

    add(-2 * x(0) + 6 * x(1) - 3 * x(2) + 4 * x(3) + x(4) - 2 * x(5) >:= 2)
    add(-5 * x(0) - 3 * x(1) + x(2) + 3 * x(3) - 2 * x(4) + x(5) >:= -2)
    add(5 * x(0) - x(1) + 4 * x(2) -2 * x(3) + 2 * x(4) - x(5) >:= 3)

    it ("all variables should be binary") {
      x.foreach(_.isBinary shouldBe true)
    }

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 11") {
      objectiveValue shouldEqual 11
    }

    release()
  }

  // Quadratic objective function tests

  describe("Quadratic Program (1)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    var cons = Vector.empty[MPConstraint]

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)

    minimize(-8*x - 16*y + x*x + 4*y*y)

    cons = cons :+ add(x + y <:= 5)
    cons = cons :+ add(x <:= 3)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to -31") {
      objectiveValue shouldEqual -31.0 +- 1e-2
    }

    it("x should be equal to 3") {
      x.value.get shouldEqual 3.0 +- 1e-2
    }

    it("y should be equal to 2") {
      y.value.get shouldEqual 2.0 +- 1.0e-2 // Here gurobi requires +- 1.0e-4
    }

    it("constraints should be satisfied") {
      cons(0).isTight() shouldBe false
      cons(1).isTight() shouldBe true

      cons(0).slack.get shouldBe 0.0 +- 1.0e-2
      cons(1).slack.get shouldBe 0.0 +- 1.0e-2

      cons.foreach(_.check() shouldBe true)
    }

    release()
  }

  describe("Quadratic Program (2)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    var cons = Vector.empty[MPConstraint]

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)

    minimize(2*x*x + y*y + x*y + x + y)

    cons = cons :+ add(x + y := 1)
    cons = cons :+ add(x >:= -3)
    cons = cons :+ add(y >:= -1e-4)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 1.87") {
      objectiveValue shouldEqual 1.87 +- 1.0e-2
    }

    it("x should be equal to 0.25") {
      x.value.get shouldEqual 0.25 +- 1e-2
    }

    it("y should be equal to 0.75") {
      y.value.get shouldEqual 0.75 +- 1e-2
    }

    it("constraints should be satisfied") {
      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe false
      cons(2).isTight() shouldBe false

      cons(0).slack.get shouldBe 0.0 +- 1e-2
      cons(1).slack.get shouldBe 3.25 +- 1e-2
      cons(2).slack.get shouldBe 0.75 +- 1e-2

      cons.foreach(_.check() shouldBe true)
    }

    release()
  }

  describe("Quadratic Program (3)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    var cons = Vector.empty[MPConstraint]

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)

    minimize(x*x + x*x + y*y - y*y + y*y + 7*x*y - 6*y*x + x*x - x*x + x - 99.9e-9*y + 1.0000000999*y)

    cons = cons :+ add(x + y := 1)
    cons = cons :+ add(x >:= -3)
    cons = cons :+ add(y >:= -1e-4)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 1.87") {
      objectiveValue shouldEqual 1.87 +- 1e-2
    }

    it("x should be equal to 0.25") {
      x.value.get shouldEqual 0.25 +- 1e-2
    }

    it("y should be equal to 0.75") {
      y.value.get shouldEqual 0.75 +- 1e-2
    }

    it("constraints should be satisfied") {
      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe false
      cons(2).isTight() shouldBe false

      cons(0).slack.get shouldBe 0.0 +- 1e-2
      cons(1).slack.get shouldBe 3.25 +- 1e-2
      cons(2).slack.get shouldBe 0.75 +- 1e-2

      cons.foreach(_.check() shouldBe true)
    }

    release()
  }

  describe("Quadratic Program (4)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)

    minimize(-8*x - 16*y + x*x + 4*y*y)
    subjectTo (
      x + y <:= 5,
      x <:= 3,
      x >:= 0,
      y >:= 0
    )

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to -31") {
      objectiveValue shouldEqual -31.0 +- 1e-2
    }

    it("x should be equal to 3") {
      x.value.get shouldEqual 3.0 +- 1e-2
    }

    it("y should be equal to 2") {
      y.value.get shouldEqual 2.0 +- 1e-2
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }

  describe("Quadratic Program (5)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    val w0 = MPFloatVar("w0", Double.NegativeInfinity, Double.PositiveInfinity)
    val w1 = MPFloatVar("w1", Double.NegativeInfinity, Double.PositiveInfinity)
    val w2 = MPFloatVar("w2", Double.NegativeInfinity, Double.PositiveInfinity)
    val slack = MPFloatVar("slack", 0, Double.PositiveInfinity)

    minimize(0.5*(w0*w0 + w1*w1 + w2*w2) + 1000*slack)
    add(-2.0*w2 + 0.0 >:= -1.0*slack + 16.0)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 32") {
      objectiveValue shouldEqual 32.0 +- 1e-2
    }

    it("w0 and w1 should be equal to 0") {
      w0.value.get shouldEqual 0.0 +- 1e-2
      w1.value.get shouldEqual 0.0 +- 1e-2
    }

    it("w2 should be equal to -8") {
      w2.value.get shouldEqual -8.0 +- 1e-2
    }

    it("slack should be equal to 0") {
      slack.value.get shouldEqual 0.0 +- 1e-2
    }

    it("constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    it("Add a couple of constraints and re-optimize") {

      add(-2.0*w1 + -2.0*w0 + 6.0*w2 + 0.0 >:= -1.0*slack + 6.0)
      start()

      status shouldBe ProblemStatus.OPTIMAL
      objectiveValue shouldBe 214.25 +- 1e-2

      w0.value.get shouldEqual -13.5 +- 1e-2
      w1.value.get shouldEqual -13.5 +- 1e-2
      w2.value.get shouldEqual -8.0 +- 1e-2
      slack.value.get shouldEqual 0.0 +- 1e-2

      checkConstraints() shouldBe true
    }
  }

  // Quadratic objective function tests having quadratic constraints

  describe("Quadratic Constraint Program (1)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", Double.NegativeInfinity, Double.PositiveInfinity)
    val y = MPFloatVar("y", -0.5, 0.5)

    maximize(x)
    add(x*x + y*y <:= 1)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 1") {
      objectiveValue shouldEqual 1.0 +- 1e-2
    }

    it("x should be equal to 1") {
      x.value.get shouldEqual 1.0
    }

    it("y should be equal to 0") {
      y.value.get shouldEqual 0.0
    }

    release()
  }

  describe("Quadratic Constraint Program (2)") {

    implicit val qp: LQProblem = LQProblem(SolverLib.gurobi)

    val x = MPFloatVar("x", 0, Double.PositiveInfinity)
    val y = MPFloatVar("y", 0, Double.PositiveInfinity)
    val z = MPFloatVar("z", 0, Double.PositiveInfinity)

    minimize(x*x + 0.1*y*y + z*z - x*z + y)
    add(x + y + z - x*x - y*y - 0.1*z*z + 0.2*x*z >:= 1)

    start()

    it("solution should be optimal") {
      status shouldBe ProblemStatus.OPTIMAL
    }

    it("objective value should be equal to 0.41") {
      objectiveValue shouldEqual 0.41 +- 1e-2
    }

    it("x should be equal to 1") {
      x.value.get shouldEqual 0.46 +- 1e-2
    }

    it("y should be equal to 0") {
      y.value.get shouldEqual 0.01 +- 1e-2
    }

    release()
  }
}
