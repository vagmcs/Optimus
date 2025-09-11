package optimus.optimization

import optimus.algebra._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.MPFloatVar

/**
 * The goal of the diet problem is to find the cheapest combination of foods
 * that satisfy all the daily nutritional requirements of a person. The problem
 * is formulated as a linear program where the objective is to minimize the cost
 * and meet constraints which require that nutritional needs be satisfied.
 *
 * We include constraints that regulate the number of calories and the amounts
 * of vitamins, minerals, fats, sodium and cholesterol in the diet.
 */
trait Diet extends AnyFunSpec with Matchers {

  def solver: SolverLib

  implicit val dietProblem: MPModel = MPModel(solver)

  case class Nutriment(name: String)
  case class Food(x: MPFloatVar, price: Double, contents: Nutriment => Double)

  private val nutriments: List[Nutriment] = List("A", "C", "B1", "B2", "NA", "CAL").map(Nutriment.apply)

  // Each food is limited between 2 and 10
  private def createVar(name: String) = MPFloatVar(name, 2, 10)

  val foods: List[Food] = List(
    (createVar("Beef"), 3.19, List(60, 20, 10, 15, 938, 295)),
    (createVar("Chicken"), 2.59, List(8, 0, 20, 20, 2180, 770)),
    (createVar("Fish"), 2.29, List(8, 10, 15, 10, 945, 440)),
    (createVar("Ham"), 2.89, List(40, 40, 35, 10, 278, 430)),
    (createVar("Macaroni"), 1.89, List(15, 35, 15, 15, 1182, 315)),
    (createVar("MeatLoaf"), 1.9, List(70, 30, 15, 15, 896, 400)),
    (createVar("Spaghetti"), 1.99, List(25, 50, 25, 15, 1329, 370)),
    (createVar("Turkey"), 2.49, List(60, 20, 15, 10, 1397, 450))
  ).map { case (n, p, nut) => Food(n, p, nutriments.zip(nut.map(_.toDouble)).toMap) }

  describe("Diet Problem") {

    // for each nutriment, at least 700 must be present in the Diet
    for (n <- nutriments) add(sum(foods)(f => f.x * f.contents(n)) >:= 700)

    // minimize the total cost
    minimize(sum(foods)(f => f.x * f.price))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver total utility should be 100 +- 0.2") {
      objectiveValue shouldEqual 100.0 +- 2e-1
    }

    it("all variables should have a value") {
      foods.foreach(_.x.value.isDefined shouldBe true)
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    println(foods.map(f => s"${f.x.symbol} -> ${f.x.value.get}").mkString("\n"))

    release()
  }
}
