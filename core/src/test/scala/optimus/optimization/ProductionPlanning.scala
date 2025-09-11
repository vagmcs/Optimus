package optimus.optimization

import optimus.algebra._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.MPFloatVar

/**
 * A number of 12 products can be produced. Each of them has a set of features,
 * such as volume, weight, etc. There is a capacity constraint on the total amount
 * that can be produced from each feature; for instance, an upper limit of the
 * total weight of the produced products. Moreover, each product generates a profit
 * per unit produced. The objective is to maximize the total profit, while
 * satisfying these capacity constraints.
 */
trait ProductionPlanning extends AnyFunSpec with Matchers {

  def solver: SolverLib

  describe("Production Planning Problem") {

    implicit val ppProblem: MPModel = MPModel(solver)

    // dimensions
    val b = Array(18209, 7692, 1333, 924, 26638, 61188, 13360)

    // products
    val c = Array(96, 76, 56, 11, 86, 10, 66, 86, 83, 12, 9, 81)

    val dimensions = b.indices
    val products = c.indices

    val w = Array(
      Array(19, 1, 10, 1, 1, 14, 152, 11, 1, 1, 1, 1),
      Array(0, 4, 53, 0, 0, 80, 0, 4, 5, 0, 0, 0),
      Array(4, 660, 3, 0, 30, 0, 3, 0, 4, 90, 0, 0),
      Array(7, 0, 18, 6, 770, 330, 7, 0, 0, 6, 0, 0),
      Array(0, 20, 0, 4, 52, 3, 0, 0, 0, 5, 4, 0),
      Array(0, 0, 40, 70, 4, 63, 0, 0, 60, 0, 4, 0),
      Array(0, 32, 0, 0, 0, 5, 0, 3, 0, 660, 0, 9)
    )

    val x = products.map(p => MPFloatVar(s"x$p", 0, 10000))

    maximize(sum(products)(p => x(p) * c(p)))

    for (d <- dimensions) add(sum(products)(p => x(p) * w(d)(p)) <:= b(d))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 261972 +- 0.5") {
      objectiveValue shouldBe 261972.0 +- 5e-1
    }

    it("all variables should have a value") {
      x.foreach(_.value.isDefined shouldBe true)
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    release()
  }
}
