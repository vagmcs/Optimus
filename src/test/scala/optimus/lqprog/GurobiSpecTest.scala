package optimus.lqprog

import org.scalatest.{FunSpec, Matchers}

/**
 * Specification for Gurobi solver.
 *
 * @author Anastasios Skarlatidis
 * @author Vagelis Michelioudakis
 * @author Christos Vlassopoulos
 */
final class GurobiSpecTest extends FunSpec with Matchers {

  describe("Linear programming") {

    describe("Test I") {
      implicit val problem = new LQProblem(SolverLib.gurobi)

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
      implicit val problem = new LQProblem(SolverLib.gurobi)

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
      implicit val lp = new LQProblem(SolverLib.gurobi)

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
      implicit val lp = new LQProblem(SolverLib.gurobi)

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
      implicit val lp = new LQProblem(SolverLib.gurobi)

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
      implicit val lp = new LQProblem(SolverLib.gurobi)

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
      implicit val lp = new LQProblem(SolverLib.gurobi)

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

    describe("Test VIII") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      val w = MPFloatVar("w", 0, Double.PositiveInfinity)
      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      var cons: Vector[MPConstraint] = Vector()

      // Max 5w + x + 3y + 4z
      maximize(3*w - 8*w + 10*w + 0.001*x - (-0.999*x) - 0.3*10*(-y) - 4*0.0006*0*(w-x-z) + 2*z - 2*z + 4*z)
//      subjectTo( w + x + y + z <= 40,
//        2*w + x - y - z >= 10 )

      cons = cons :+ add(w + x + y + z <= 40)
      cons = cons :+ add(2*w + x - y - z >= 10)

      start()

      w.value.get should equal (4.0e+1 +- 1.0e-6)
      x.value.get should equal (0.0 +- 1.0e-6)
      y.value.get should equal (0.0 +- 1.0e-6)
      z.value.get should equal (0.0 +- 1.0e-6)
      objectiveValue should be(2.0e+2 +- 1.0e-6)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(false)

      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be (true)

      //TODO: Constraint of the form y >= w doesn't work. Seen as boolean.
      cons = cons :+ add(y - w >= 0)
      cons = cons :+ add(x >= 15)
      start()

      w.value.get should equal (12.5 +- 1.0e-6)   //  6.66666667
      x.value.get should equal (1.5e+1 +- 1.0e-6)
      y.value.get should equal (12.5 +- 1.0e-6)   //  6.66666667
      z.value.get should equal (0.0 +- 1.0e-6)    // 11.66666667
      objectiveValue should be(1.15e+2 +- 1.0e-6)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(false)
      cons(2).isTight() should be(true)
      cons(3).isTight() should be(true)

      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be (true)

      // Constraint: w - 2x + 4y + 3z >= 40
      cons = cons :+ add(-(-w) - 2*x + 4*y + 3*0.5*2*z >= 40 - 3 + 2.7 + 0.3)
      start()

      w.value.get should equal (6.66666667 +- 1.0e-6)
      x.value.get should equal (1.5e+1 +- 1.0e-6)
      y.value.get should equal (8.33333333 +- 1.0e-6)
      z.value.get should equal (1.0e+1 +- 1.0e-6)
      objectiveValue should be(1.1333333333e+2 +- 1.0e-6)
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be (true)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(true)
      cons(2).isTight() should be(false)
      cons(3).isTight() should be(true)
      cons(4).isTight() should be(true)

      release()
    }
  }

  describe("Quadratic programming") {

    describe("Test I") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      val x = MPFloatVar("x", Double.NegativeInfinity, Double.PositiveInfinity)
      val y = MPFloatVar("y", -0.5, 0.5)

      maximize(x)
      add(x*x + y*y <= 1)
      start()

      x.value should equal (Some(1.0))
      y.value should equal (Some(0.0))
      objectiveValue should be(1.0 +- 0.0001)
      status should equal(ProblemStatus.OPTIMAL)
    }

    describe("Test II") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      minimize(x*x + 0.1*y*y + z*z - x*z + y)
      add(x + y + z - x*x - y*y - 0.1*z*z + 0.2*x*z >= 1)
      start()

      x.value.get should equal (0.4682428458167764 +- 0.0001)
      y.value.get should equal (0.012630817746014237 +- 0.0001)
      objectiveValue should be(0.4161924543450218 +- 0.0001)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test IIΙ") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(-8*x - 16*y + x*x + 4*y*y)

      cons = cons :+ add(x + y <= 5)
      cons = cons :+ add(x <= 3)
      start()

      x.value.get should equal (3.0 +- 1.0e-6)
      y.value.get should equal (2.0 +- 1.0e-4)  // imprecise!

      cons(0).isTight() should be(false)  // !
      cons(0).isTight(10e-4) should be(true)  // !
      cons(1).isTight() should be(true)

      cons(0).slack should be(0.0 +- 1.0e-4)  // !
      cons(1).slack should be(0.0 +- 1.0e-6)

      cons.foreach(c => c.check() should be(true))

      objectiveValue should be(-31.0 +- 1.0e-4) // !
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test IV") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(2*x*x + y*y + x*y + x + y)
      cons = cons :+ add(x + y := 1)
      cons = cons :+ add(x >= -3)
      cons = cons :+ add(y >= -1.0e-4)
      start()

      x.value.get should equal (2.5e-1 +- 1.0e-6)
      y.value.get should equal (7.5e-1 +- 1.0e-6)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(false)
      cons(2).isTight() should be(false)

      cons(0).slack should be(0.0 +- 1.0e-6)
      cons(1).slack should be(3.25 +- 1.0e-6)
      cons(2).slack should be(7.501e-1 +- 1.0e-6)

      cons.foreach(c => c.check() should be(true))

      objectiveValue should be(1.875 +- 1.0e-6)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test V") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      var cons: Vector[MPConstraint] = Vector()

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(x*x + x*x + y*y - y*y + y*y + 7*x*y - 6*y*x + x*x - x*x + x - 99.9e-9*y + 1.0000000999*y)
      cons = cons :+ add(x + y := 1)
      cons = cons :+ add(x >= -3)
      cons = cons :+ add(y >= -1.0e-4)
      start()

      x.value.get should equal (2.5e-1 +- 1.0e-6)
      y.value.get should equal (7.5e-1 +- 1.0e-6)

      cons(0).isTight() should be(true)
      cons(1).isTight() should be(false)
      cons(2).isTight() should be(false)

      cons(0).slack should be(0.0 +- 1.0e-6)
      cons(1).slack should be(3.25 +- 1.0e-6)
      cons(2).slack should be(7.501e-1 +- 1.0e-6)

      cons.foreach(c => c.check() should be(true))

      objectiveValue should be(1.875 +- 1.0e-6)
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    /**
     * Almost identical to III
     */
    describe("Test VI") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)

      minimize(-8*x - 16*y + x*x + 4*y*y)
      subjectTo( x + y <= 5,
        x <= 3,
        x >= 0,
        y >= 0 )
      start()

      x.value.get should equal (2.9999999998374056 +- 0.0001)
      y.value.get should equal (1.999958833749785 +- 0.0001)
      objectiveValue should be(-3.10000000e+01 +- 0.0001)
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be(true)

      release()
    }

    /**
     * Fails on OJalgo!
     */
    describe("Test VII") {
      implicit val lp = new LQProblem(SolverLib.gurobi)

      val w0 = MPFloatVar("w0", Double.NegativeInfinity, Double.PositiveInfinity)
      val w1 = MPFloatVar("w1", Double.NegativeInfinity, Double.PositiveInfinity)
      val w2 = MPFloatVar("w2", Double.NegativeInfinity, Double.PositiveInfinity)
      val slack = MPFloatVar("slack", 0, Double.PositiveInfinity)

      minimize(0.5*(w0*w0 + w1*w1 + w2*w2) + 1000*slack)

      add(-2.0*w2 + 0.0 >= -1.0*slack + 16.0)
      start()

      w0.value.get should equal (0.0 +- 0.0001)
      w1.value.get should equal (0.0 +- 0.0001)
      w2.value.get should equal (-8.0 +- 0.0001)
      slack.value.get should equal (4.642039067684605E-16 +- 0.0001)
      objectiveValue should be(3.20000000e+01 +- 0.0001)
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be (true)

      add(-2.0*w1 + -2.0*w0 + 6.0*w2 + 0.0 >= -1.0*slack + 6.0)
      start()

      w0.value.get should equal (-13.5 +- 0.0001)
      w1.value.get should equal (-13.5 +- 0.0001)
      w2.value.get should equal (-8.0 +- 0.0001)
      slack.value.get should equal (8.241638881309338E-15 +- 0.0001)
      objectiveValue should be(2.14250000e+02 +- 0.0001)
      status should equal(ProblemStatus.OPTIMAL)
      checkConstraints() should be (true)

      release()
    }
  }

  println()

}
