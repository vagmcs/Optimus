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
 * The mathematical programming library for Scala.
 *
 */

package optimus.optimization

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.MPFloatVar

/**
  * The Maximum Flow Problem in a network G = (V, E), where V is a set of nodes,
  * E within V x V is a set of arcs, is to maximize the flow from one given
  * node s (source) to another given node t (sink) subject to conservation of
  * flow constraints at each node and flow capacities on each arc.
  */
trait MaxFlow extends AnyFunSpec with Matchers {

  def solver: SolverLib

  describe("Max flow problem") {

    implicit val maxFlowProblem: MPModel = MPModel(solver)

    val lines = 0 to 7
    val columns = 0 to 8
    val capacities = Array(
      Array(0, 12, 0, 23, 0, 0, 0, 0, 0),
      Array(0, 0, 10, 9, 0, 0, 0, 0, 0),
      Array(0, 0, 0, 0, 12, 0, 0, 18, 0),
      Array(0, 0, 0, 0, 26, 0, 0, 0, 0),
      Array(0, 11, 0, 0, 0, 25, 4, 0, 0),
      Array(0, 0, 0, 0, 0, 0, 7, 8, 0),
      Array(0, 0, 0, 0, 0, 0, 0, 0, 15),
      Array(0, 0, 0, 0, 0, 63, 0, 0, 20)
    )

    val x = Array.tabulate(lines.size, columns.size)(
      (l, c) => MPFloatVar(s"x($l,$c)", 0, capacities(l)(c))
    )

    for (l <- 1 until lines.size)
      add(sum(columns)(c => x(l)(c)) - sum(lines)(c => x(c)(l)) := 0)

    maximize(sum(lines)(l => x(l)(columns.size - 1)))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 29") {
      objectiveValue shouldBe 29.0 +- 1e-2
    }

    it("all variables should have a value") {
      for (l <- lines; c <- columns)
        x(l)(c).value.isDefined shouldBe true
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    for (l <- lines) {
      for (c <- columns)
        print(s"${x(l)(c).value.get} ")
      println
    }

    release()
  }
}
