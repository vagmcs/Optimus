package optimus.optimization

import org.scalatest.{Matchers, FunSpec}

/**
 * @author Vagelis Michelioudakis 
 */
final class ProblemSpecTest extends FunSpec with Matchers {

  implicit val problem = new LQProblem(SolverLib.lp_solve)
  
  val y = MPIntVar(0 to 100)
  val x = MPIntVar("a", 0 to 1)
  
  val z = MPFloatVar(false)
  
  info(""+x.isBinary)
  info(""+x.isInteger)

  val startv = System.currentTimeMillis()
  var variables = Vector[MPFloatVar]()
  for(i <- 1 to 100000)
    variables :+= MPFloatVar("x"+i, 0, 1)
  println("Time: " + (System.currentTimeMillis() - startv))

  val startc = System.currentTimeMillis()
  for(i <- 0 until variables.length)
    add(variables(i) >= 0)
  println("Time: " + (System.currentTimeMillis() - startc))
  
}
