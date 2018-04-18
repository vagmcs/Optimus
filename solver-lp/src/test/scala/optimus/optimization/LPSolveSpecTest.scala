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
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus.optimization

import optimus.optimization.enums.{SolutionStatus, SolverLib}
import optimus.optimization.model.{MPConstraint, MPFloatVar, MPIntVar}
import org.scalatest.{FunSpec, Matchers}

/**
  * Specification for LPSolve solver.
  */
final class LPSolveSpecTest extends FunSpec with Matchers {

  describe("Constant Program (1)") {

    implicit val cp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    maximize(-5)

    add(x >:= 5)
    add(y <:= 100)

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("x should be equal to 100") {
      x.value shouldEqual Some(100)
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

    implicit val cp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-5)

    add(x >:= 150)
    add(y <:= 100)

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("x should be equal to 150") {
      x.value shouldEqual Some(150)
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

  describe("Linear Program (1)") {

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    maximize(-2 * x + 5 * y)
    subjectTo (
      y >:= -x + 200
    )

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
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

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo (
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

  describe("Linear Program (3)") {

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x")
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)
    subjectTo (
      y >:= -x + 200
    )

    start()

    // Solution is infeasible but some solvers consider it dual infeasible
    it("solution should be infeasible") {
      status should (equal(SolutionStatus.UNBOUNDED) or equal(SolutionStatus.INFEASIBLE))
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

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)

    minimize(-2 * x + 5 * y)

    val z = MPFloatVar("z", 80, 170)

    subjectTo (
      z >:= 170,
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

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

    val x = MPFloatVar("x", 0, 10)
    val y = MPFloatVar("y", 0, 10)

    maximize(x + y)
    subjectTo (
      x + y >:= 5
    )

    start()

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
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

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

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

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
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

      cons.foreach(c => c.check() shouldBe true)
    }

    release()
  }

  describe("Linear Program (7)") {

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

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

    it("solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
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
      cons.head.isTight() shouldBe true
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

      cons.foreach(c => c.check() shouldBe true)
    }

    release()
  }

  describe("Linear Program (8)") {

    implicit val lp: MPModel = MPModel(SolverLib.LpSolve)

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
      status shouldBe SolutionStatus.OPTIMAL
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

      status shouldEqual SolutionStatus.OPTIMAL
      checkConstraints() shouldBe true

      // Constraint: w - 2x + 4y + 3z >:= 40
      cons :+= add(-(-w) - 2 * x + 4 * y + 3 * 0.5 * 2 * z >:= 40 - 3 + 2.7 + 0.3)
      start()

      w.value.get should equal(6.67 +- 1e-2)
      x.value.get should equal(15.0 +- 1e-2)
      y.value.get should equal(8.33 +- 1e-2)
      z.value.get should equal(10.0 +- 1e-2)
      objectiveValue shouldBe (113.33 +- 1e-2)
      status should equal(SolutionStatus.OPTIMAL)
      checkConstraints() shouldBe true

      cons(0).isTight() shouldBe true
      cons(1).isTight() shouldBe true
      cons(2).isTight() shouldBe false
      cons(3).isTight() shouldBe true
      cons(4).isTight() shouldBe true

      release()
    }

  }

  // Mixed-integer objective function tests

  describe("Mixed-Integer Program (1)") {

    implicit val mip: MPModel = MPModel(SolverLib.LpSolve)

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
      status shouldBe SolutionStatus.OPTIMAL
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

    implicit val mip: MPModel = MPModel(SolverLib.LpSolve)

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

  describe("Mixed-Integer Program (3)") {

    implicit val mip: MPModel = MPModel(SolverLib.LpSolve)

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
      status shouldBe SolutionStatus.OPTIMAL
    }

    it("objective value should be equal to 11") {
      objectiveValue shouldEqual 11
    }

    release()
  }
}
