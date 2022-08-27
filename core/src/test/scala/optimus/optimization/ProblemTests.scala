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

import optimus.optimization.enums.SolverLib

/**
  * A set of MIP problem tests that should be implemented
  * by any MIP solver to test their proper functionality.
  */
trait MIPProblemTests
  extends Knapsack
  with Queens
  with Sudoku
  with Warehouse
  with Workforce {

  def solver: SolverLib
}

/**
  * A set of LP problem tests that should be implemented
  * by any LP solver to test their proper functionality.
  */
trait LPProblemTests
  extends Diet
  with MaxFlow
  with ProductionPlanning {

  def solver: SolverLib
}

/**
  * A set of all the above problem tests that should be implemented
  * by any LP/MIP solver to test their proper functionality.
  */
trait ProblemTests
  extends LPProblemTests
  with MIPProblemTests {

  def solver: SolverLib
}
