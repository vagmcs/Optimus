package optimus.algebra

import org.scalatest.{Matchers, FunSpec}
import optimus.optimization.{LQProblem, MPFloatVar}

/**
* Specification for algebra.
*
* @author Vagelis Michelioudakis
* @author Christos Vlassopoulos
*/
final class AlgebraSpecTest extends FunSpec with Matchers {

  implicit val problem = LQProblem()

  /**
   * Definition of variables
   */
  val x = MPFloatVar("x", 0.0, 1.0)
  val y = MPFloatVar("y", 3.5, 100)
  val z = MPFloatVar("z", true)
  val t = MPFloatVar("t")

  describe("Domain of variables") {

    x.lowerBound should equal (0.0)
    x.upperBound should equal (1.0)
    x.isUnbounded should equal (false)

    y.lowerBound should equal (3.5)
    y.upperBound should equal (100)
    y.isUnbounded should equal (false)

    z.lowerBound should equal (Double.PositiveInfinity)
    z.upperBound should equal (Double.PositiveInfinity)
    z.isUnbounded should equal (true)

    t.lowerBound should equal (0.0)
    t.upperBound should equal (Double.PositiveInfinity)
    t.isUnbounded should equal (false)
  }

  describe("Equality of expressions") {

    /**
     * Checking variable properties
     */
    info(x - 0 + " should be equal to " + (x + 0))
    x - 0 should equal (x + 0)

    info(x*1.0 + " should be equal to " + -(-x))
    x*1.0 should equal (-(-x))

    info(x*(-1.0) + " should be equal to " + -x)
    x*(-1.0) should equal (-x)

    info(0 - x + " should be equal to " + (-x))
    0 - x should equal (-x)

    info(-x + " should be equal to " + -(-(-x)))
    -x should equal ( -(-(-x)) )

    info(x * -5 + " should be equal to " + -5.0 * x)
    x * -4.2 should equal (-4.2 * x)
    x * -5 should equal (-5.0 * x)

    /**
     * Checking expression term properties
     */
    info(2.1 * x * y * z + " should be equal to " + y * x * z * 2.1)
    2.1 * x * y * z should equal (y * x * z * 2.1)

    info(x * (-7.7) * y * z + " should be equal to " + y * z * x * (-7.7))
    x * (-7.7) * y * z should equal (y * z * x * (-7.7))
    
    info((x^2) + " should be equal to " + x * x)
    x^2 should equal (x * x)

    info((x^4) + " should be equal to " + x * x * x * x)
    x^4 should equal (x * x * x * x)

    info((x^0) + " should be equal to " + One)
    x^0 should equal (One)
    
    /**
     * Checking complex expression properties
     */
    info(2.1 * x * y + 3.2 * z * t + " should be equal to " + (2.1 * y * x + t * z * 3.2))
    2.1 * x * y + 3.2 * z * t should equal (2.1 * y * x + t * z * 3.2)

    info(2.1 * x * y + 3.9 * z * t + 9 + " should be equal to " + (9.0 + 2.1 * x * y + 3.9 * z * t))
    2.1 * x * y + 3.9 * z * t + 9 should equal (9.0 + 2.1 * x * y + 3.9 * z * t)

    /**
     * Checking sum function over iterable
     */
    val expression1 = 2 * x * y + 2 * z * t
    val expression2 = 2 * x * z + 4 * t + 5.0
    val expression3 = 3 * x * y * t + z + y * z
    val expression4 = 4 + z
    val expressions = Array(expression1, expression2, expression3, expression4)
    val expressions12 = Array(expression1, expression2)
    val expressions34 = Array(expression3, expression4)

    /**
     * Produces 2.0(x * y) + 2.0(z * t) + 2.0(x * z) + 4.0t + 5.0
     */
    info(expression1 + expression2 + " should be equal to " + sum(expressions12))
    expression1 + expression2 should equal (sum(expressions12))

    /**
     * Produces 3.0(x * y * t) + 1.0(y * z) + 2.0z + 4.0
     */
    info(expression3 + expression4 + " should be equal to " + sum(expressions34))
    expression3 + expression4 should equal (sum(expressions34))

    /**
     * Produces 3.0(x * y * t) + 2.0(x * y) + 2.0(x * z) + 1.0(y * z) + 2.0(z * t) + 2.0z + 4.0t + 9.0
     */
    info(expression1 + expression2 + expression3 + expression4 + " should be equal to " + sum(expressions))
    expression1 + expression2 + expression3 + expression4 should equal (sum(expressions))
  }

  describe("Simplification of expressions") {

    info(5*(x + y * z - t) + 3.0 + " should be equal to " + (5 * z * y + 5 * x - 5 * t + 3))
    5*(x + y * z - t) + 3 should equal (5 * z * y + 5 * x - 5 * t + 3)

    info(x - x + " should equal to " + 0.0)
    x - x should equal (Zero)

    info(-1 * x + x + " should equal to " + 0.0)
    -1 * x + x should equal (Zero)

    info(-x + x + y + " should equal to " + 1 * y)
    -x + x + y should equal (1.0*y)

    info(x + x + " should equal to " + 2 * x)
    x + x should equal (2 * x)

    info(4 * t + 3 * t + " should equal to " + 7 * t)
    4 * t + 3 * t should equal (7 * t)

    info(2 * x - 4 * x + " should equal to " + -2 * x)
    2 * x - 4 * x should equal (-2 * x)

    info(2 * x - 2 * x + " should equal to " + 0.0)
    2 * x - 2 * x should equal (Zero)

    info(5 * x - x + " should equal to " + 4 * x)
    5 * x - x should equal (4 * x)

    info(-x + 4 * y + x + " should equal to " + 4 * y)
    -x + 4 * y + x should equal (4 * y)

    info((x + y) + (2 * x + y) + " should equal to " + (3 * x + 2 * y))
    (x + y) + (2 * x + y) should equal (3 * x + 2 * y)

    info((t + z) - (t + z) + " should equal to " + 0.0)
    (t + z) - (t + z) should equal (Zero)

    info((z - t) + (t - z) + " should equal to " + 0.0)
    (z - t) + (t - z) should equal (Zero)

    info((x + 5) * (x + 4) + " should equal to " + (x*x + 9*x + 20))
    (x + 5) * (x + 4) should equal (x*x + 9*x + 20)

    val expr_0 = -2*z - 2*x - y + z + x - 1 + 1*z // equals -x1 - x2 - 1
    expr_0.equals(-x - y - 1) should equal (true)

    val expr_1 = 1 - (expr_0 - 1) + 2 - 1 // 4 + x1 + x2
    expr_1.equals(4 + x + y) should equal (true)

    val expr_2 = -expr_1 // -4 - x1 - x2
    expr_2.equals(-4 - x - y) should equal (true)

    expr_2.equals( -4 - sum(Vector(x, y)) )
  }

  describe("Order of expressions") {

    (x - x).getOrder should equal(ExpressionOrder.CONSTANT)

    (x + t + -5*y + 2*t + -3.2*z).getOrder should equal(ExpressionOrder.LINEAR)

    (x*y + z*t + 5*y + 2*x*t + z*z).getOrder should equal(ExpressionOrder.QUADRATIC)

    (x*y*z + z*t + 5*y + 2*x*t + x*z*z).getOrder should equal(ExpressionOrder.HIGHER)

    (x*y*z + z*t + 5*y + 2*x*t + x*z*y*z).getOrder should equal(ExpressionOrder.HIGHER)
  }

  describe("Summation and product having many variables") {

    val variables = Array.tabulate(100000)(i => MPFloatVar(i.toString, 0, 1))

    val startSum = System.currentTimeMillis()
    sum(variables)
    info("Summation of " + variables.length + " variables took " + (System.currentTimeMillis() - startSum) + "ms to calculate")

    val startProd = System.currentTimeMillis()
    val expr = (x + y + x + y + t + z + t + z + 4.1*y + x + 5) * (x + y + x + y + t + z + t + z + y + x + 2)
    info("Product of " + expr + " took " + (System.currentTimeMillis() - startProd) + "ms to calculate")
  }

  describe("Constraints") {

    info( (x*z + 5.7*x - 34*t >= x) + ", " + (x*x*x*x + z*t + 9.1 := y*t + 8.7*z))
  }

  println() // end specification
}
