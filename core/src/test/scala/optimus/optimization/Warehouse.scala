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

import org.scalatest.{ FunSpec, Matchers }
import optimus.algebra.AlgebraOps._
import optimus.optimization.enums.{ SolutionStatus, SolverLib }
import optimus.optimization.model.{ MPBinaryVar, MPFloatVar, MPIntVar }

/**
  * Facility Location Problem
  *
  * There is a set of plants that can be open or not having
  * a capacity and a set of warehouses.
  *
  * - Each warehouse has a demand that must be satisfied by one or more plant.
  * - There is a cost specified for shipping one unit from a particular plant
  *   to a particular warehouse and a cost for opening a plant.
  *
  * The objective is to minimize the total cost while satisfying the demand
  * of the warehouses and the capacities of the plant.
  */
trait Warehouse extends FunSpec with Matchers {

  def solver: SolverLib

  describe("Warehouse Problem") {

    implicit val warehouseProblem: MPModel = MPModel(solver)

    // Warehouse demand in thousands of units
    val demand = Array(15, 18, 14, 20)

    // Plant capacity in thousands of units
    val capacity = Array(20, 22, 17, 19, 18)

    // Fixed costs for each plant
    val fixedCosts = Array(12000, 15000, 17000, 13000, 16000)

    // Transportation costs per thousand units
    val transCosts = Array(
      Array(4000, 2000, 3000, 2500, 4500),
      Array(2500, 2600, 3400, 3000, 4000),
      Array(1200, 1800, 2600, 4100, 3000),
      Array(2200, 2600, 3100, 3700, 3200)
    )

    // Number of plants and warehouses
    val plants = capacity.indices
    val warehouses = demand.indices

    // For each plant whether it is open (1) or not (0)
    val open = plants.map(p => MPBinaryVar(s"open$p"))

    // Transportation decision variables: how much to transport from a plant 'p' to a warehouse 'w'
    val transport = Array.tabulate(warehouses.length, plants.length)(
      (w, p) => MPFloatVar(s"trans($w, $p)", 0, Double.MaxValue)
    )

    // The objective is to minimize the total fixed and variable costs
    minimize(
      sum(warehouses, plants) { (w, p) => transport(w)(p) * transCosts(w)(p).toDouble } // variable cost
        + sum(plants) { p => open(p) * fixedCosts(p).toDouble } // fixed costs
    )

    // Production Constraints
    for (p <- plants)
      add(sum(warehouses)(w => transport(w)(p)) <:= open(p) * capacity(p))

    // Demand Constraints
    for (w <- warehouses)
      add(sum(plants)(p => transport(w)(p)) >:= demand(w))

    start()

    it(s"$solver solution should be optimal") {
      status shouldBe SolutionStatus.OPTIMAL
    }

    it(s"$solver objective value should be 210500") {
      objectiveValue shouldBe 210500
    }

    it("all variables should have a value") {
      transport.foreach(_.foreach(_.value.isDefined shouldBe true))
      open.foreach(_.value.isDefined shouldBe true)
    }

    it(s"$solver constraints should be satisfied") {
      checkConstraints() shouldBe true
    }

    // Print solution
    open.foreach { o =>
      println(s"$o ${o.value.get > 0}")
    }

    release()
  }
}
