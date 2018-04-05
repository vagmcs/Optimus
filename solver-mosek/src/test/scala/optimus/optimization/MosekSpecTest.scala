/*
 *
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 *  Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *       
 */

package optimus.optimization

import optimus.optimization.enums.ProblemStatus
import org.scalatest.{FunSpec, Matchers}

final class MosekSpecTest extends FunSpec with Matchers {

  describe("Constant programming") {

    describe("Test I") {
      implicit val problem = LQProblem(SolverLib.mosek)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      maximize(-5)
      add(x >:= 5)
      add(y <:= 100)
      start()

      x.value should equal(Some(100))
      y.value should equal(Some(80))
      objectiveValue should equal(-5)
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test II") {
      implicit val problem = LQProblem(SolverLib.mosek)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-5)
      add(x >:= 5)
      add(y <:= 100)
      start()

      x.value should equal(Some(100))
      y.value should equal(Some(80))
      objectiveValue should equal(-5)
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }
  }

  describe("Linear programming") {

    describe("Test I") {
      implicit val problem = LQProblem(SolverLib.mosek)
      
      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      maximize(-2 * x + 5 * y)
      add(y >:= -x + 200)
      start()

      x.value.get shouldEqual 100.0 +- 1E-5
      y.value.get shouldEqual 170d +- 1.001
      objectiveValue shouldEqual 650d +- 1.001
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test II") {
      implicit val problem = LQProblem(SolverLib.mosek)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)
      add(y >:= -x + 200)
      start()

      x.value.get shouldEqual 200d +- 1.0001
      y.value.get shouldEqual 80d +- 1.001
      objectiveValue shouldEqual 0d +- 1.001
      checkConstraints() shouldBe true
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test III") {
      implicit val lp = new LQProblem(SolverLib.mosek)

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
      implicit val lp = LQProblem(SolverLib.mosek)

      val x = MPFloatVar("x", 100, 200)
      val y = MPFloatVar("y", 80, 170)

      minimize(-2 * x + 5 * y)

      val z = MPFloatVar(lp, "z", 80, 170)

      add(z >:= 170)
      add(y >:= -x + 200)
      start()

      x.value.get shouldEqual 200d +- 1.001
      y.value.get shouldEqual 80d +- 1.001
      z.value.get shouldEqual 170d +- 1.001
      objectiveValue shouldEqual 0d +- 1.001
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }

    describe("Test V") {
      implicit val lp = LQProblem(SolverLib.mosek)

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
  }


  describe("Quadratic programming") {

    describe("Test II") {
      implicit val lp = LQProblem(SolverLib.mosek)

      val x = MPFloatVar("x", 0, Double.PositiveInfinity)
      val y = MPFloatVar("y", 0, Double.PositiveInfinity)
      val z = MPFloatVar("z", 0, Double.PositiveInfinity)

      minimize(x * x + 0.1 * y * y + z * z - x * z + y)
      add(x + y + z - x * x - y * y - 0.1 * z * z + 0.2 * x * z >:= 1)
      start()

      x.value.get shouldEqual 1.6829 +- 0.0001
      y.value.get shouldEqual 0d +- 0.0001
      objectiveValue shouldBe 0d +- 0.0001
      status should equal(ProblemStatus.OPTIMAL)

      release()
    }
  }

}
