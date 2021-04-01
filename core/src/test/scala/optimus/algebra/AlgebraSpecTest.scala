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

package optimus.algebra

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import optimus.optimization.MPModel
import optimus.algebra.AlgebraOps._
import optimus.optimization.model.{ INFINITE, MPFloatVar, MPIntVar }

/**
  * Specification test for algebra.
  */
final class AlgebraSpecTest extends AnyFunSpec with Matchers {

  implicit val model: MPModel = MPModel()

  // Definition of float variables
  private val x = MPFloatVar("x", 0, 1)
  private val y = MPFloatVar("y", 3.5, 100)
  private val z = MPFloatVar("z")
  private val t = MPFloatVar.positive("t")

  // Definition of integer variables
  private val p = MPIntVar(5 until 10)
  private val k = MPIntVar("k", 0 to 1)

  describe("Domain of variables") {

    it("Variable x is float and should be bounded to [0, 1]") {
      x.symbol shouldEqual "x"
      x.index shouldEqual 0
      x.lowerBound shouldEqual 0.0
      x.upperBound shouldEqual 1.0
      x.isInteger shouldEqual false
      x.isBinary shouldEqual false
      x.isUnbounded shouldEqual false
    }

    it("Variable y is float and should be bounded to [3.5, 100]") {
      y.symbol shouldEqual "y"
      y.index shouldEqual 1
      y.lowerBound shouldEqual 3.5
      y.upperBound shouldEqual 100
      y.isInteger shouldEqual false
      y.isBinary shouldEqual false
      y.isUnbounded shouldEqual false
    }

    it("Variable z is float and should be unbounded") {
      z.symbol shouldBe "z"
      z.index shouldEqual 2
      z.lowerBound shouldEqual INFINITE
      z.upperBound shouldEqual INFINITE
      z.isInteger shouldEqual false
      z.isBinary shouldEqual false
      z.isUnbounded shouldEqual true
    }

    it("Variable t is float and should be bounded to [0, +infinite)") {
      t.symbol shouldEqual "t"
      t.index shouldEqual 3
      t.lowerBound shouldEqual 0.0
      t.upperBound shouldEqual INFINITE
      t.isInteger shouldEqual false
      t.isBinary shouldEqual false
      t.isUnbounded shouldEqual false
    }

    it("Variable p is integer and should have domain {5...9}") {
      p.symbol shouldEqual ANONYMOUS
      p.index shouldEqual 4
      p.lowerBound shouldEqual 5
      p.upperBound shouldEqual 9
      p.isInteger shouldEqual true
      p.isBinary shouldEqual false
      p.isUnbounded shouldEqual false
    }

    it("Variable k is binary and should have domain {0...1}") {
      k.symbol shouldEqual "k"
      k.index shouldEqual 5
      k.lowerBound shouldEqual 0
      k.upperBound shouldEqual 1
      k.isInteger shouldEqual true
      k.isBinary shouldEqual true
      k.isUnbounded shouldEqual false
    }
  }

  describe("Equality of expressions") {

    // Checking variable properties

    it("x - 0 should be equal to x + 0 (check also for p variable)") {
      x - 0 shouldEqual x + 0
      p - 0 shouldEqual p + 0
    }

    it("x should be equal to -(-x)") {
      1.0 * x shouldEqual -(-x)
    }

    it("x * (-1) should be equal to -x") {
      x * (-1.0) shouldEqual -x
    }

    it("0 - x should be equal to -x") {
      0 - x shouldEqual -x
    }

    it("-x should be equal to -(-(-x))") {
      -x shouldEqual -(-(-x))
    }

    it("x * (-5) should be equal to -5 * x") {
      x * -5 shouldEqual -5.0 * x
      x * -4.2 shouldEqual -4.2 * x
    }

    // Checking expression term properties

    it("2.1 * x * y should be equal to y * x * 2.1") {
      2.1 * x * y shouldEqual y * x * 2.1
    }

    it("x * -7.7 * z should be equal to z * x * -7.7") {
      x * -7.7 * z shouldEqual z * x * -7.7
    }

    // Checking complex expression properties

    it("2.1*x*y + 3.2*z*t should be equal to 2.1*y*x + t*z*3.2") {
      2.1 * x * y + 3.2 * z * t shouldEqual 2.1 * y * x + t * z * 3.2
    }

    it("2.1*x*y + 3.9*z*t + 9 should be equal to 9 + 2.1*x*y + 3.9*z*t") {
      2.1 * x * y + 3.9 * z * t + 9 shouldEqual 9.0 + 2.1 * x * y + 3.9 * z * t
    }

    // Checking sum function over iterable

    val expression1 = 2 * x * y + 2 * z * t
    val expression2 = 2 * x * z + 4 * t + 5.0
    val expression3 = 3 * x * t + z + y * z
    val expression4 = 4 + z
    val expressions = Array(expression1, expression2, expression3, expression4)
    val expressions12 = Array(expression1, expression2)
    val expressions34 = Array(expression3, expression4)

    /*
     * Produces 2.0(x * y) + 2.0(z * t) + 2.0(x * z) + 4.0t + 5.0
     */
    it("(2*x*y + 2*z*t) + (2*x*z + 4*t + 5) should be equal to 2*x*y + 2*z*t + 2*x*z + 4*t + 5") {
      expression1 + expression2 shouldEqual sum(expressions12)
    }

    /*
     * Produces 3.0(x * t) + 1.0(y * z) + 2.0z + 4.0
     */
    it("(3*x*t + z + y*z) + 4 + z should be equal to 3*x*t + 1*y*z + 2*z + 4") {
      expression3 + expression4 shouldEqual sum(expressions34)
    }

    /*
     * Produces 3.0(x * t) + 2.0(x * y) + 2.0(x * z) + 1.0(y * z) + 2.0(z * t) + 2.0z + 4.0t + 9.0
     */
    it("(2*x*y + 2*z*t) + (2*x*z + 4*t + 5) + (3*x*t + z + y*z) + 4 + z should be equal to 3*x*t + 2*x*y + 2*x*z + 1*y*z + 2*z*t + 2*z + 4*t + 9") {
      expression1 + expression2 + expression3 + expression4 shouldEqual sum(expressions)
    }
  }

  describe("Simplification of expressions") {

    it("5*(x + y*z - t) + 3 should be simplified to 5*z*y + 5*x - 5*t + 3") {
      5 * (x + y * z - t) + 3 shouldEqual 5 * z * y + 5 * x - 5 * t + 3
    }

    it("x - x should equal to 0") {
      x - x shouldEqual Zero
    }

    it("-1*x + x should equal to 0") {
      -1 * x + x shouldEqual Zero
    }

    it("-x + x + y should equal to 1*y") {
      -x + x + y shouldEqual 1 * y
    }

    it("x + x should equal to 2*x") {
      x + x shouldEqual 2 * x
    }

    it("4*t + 3*t + t should equal to 7*t") {
      4 * t + 3 * t shouldEqual 7 * t
    }

    it("2*x -4*x should equal to -2*x") {
      2 * x - 4 * x shouldEqual -2 * x
    }

    it("2*x - 2*x should equal to 0") {
      2 * x - 2 * x shouldEqual Zero
    }

    it("5*x - x should equal to 4*x") {
      5 * x - x shouldEqual 4 * x
    }

    it("-x + 4*y + x should equal to 4*y") {
      -x + 4 * y + x should equal (4 * y)
    }

    it("(x + y) + (2*x + y) should equal to 3*x + 2*y") {
      (x + y) + (2 * x + y) shouldEqual 3 * x + 2 * y
    }

    it("(t + z) - (t + z) should equal to 0") {
      (t + z) - (t + z) shouldEqual Zero
    }

    it("(z - t) + (t - z) should equal to 0") {
      (z - t) + (t - z) shouldEqual Zero
    }

    it("(x + 5) * (x + 4) should equal to x*x + 9*x + 20") {
      (x + 5) * (x + 4) shouldEqual x * x + 9 * x + 20
    }

    val expr_0 = -2 * z - 2 * x - y + z + x - 1 + 1 * z // equals -x - y - 1
    it("expr_0 = -2*z - 2*x - y + z + x - 1 + 1*z should equal to -x - y - 1") {
      expr_0.equals(-x - y - 1) shouldEqual true
    }

    val expr_1 = 1 - (expr_0 - 1) + 2 - 1 // 4 + x + y
    it("expr_1 = 1 - (expr_0 - 1) + 2 - 1 should equal to 4 + x + y") {
      expr_1.equals(4 + x + y) shouldEqual true
    }

    val expr_2 = -expr_1 // -4 - x - y
    it("expr_2 = -expr_1 should be equal to -4 - x - y") {
      expr_2.equals(-4 - x - y) shouldEqual true
      expr_2.equals(-4 - sum(Vector(x, y))) shouldEqual true
    }
  }

  describe("Order of expressions") {

    it("x - x order should be constant") {
      (x - x).getOrder shouldEqual ExpressionType.CONSTANT
    }

    it("x*x - (x*x + y) order should be linear") {
      (x * x - (x * x + y)).getOrder shouldEqual ExpressionType.LINEAR
    }

    it("x + t + -5*y + 2*t + -3.2*z order should be linear") {
      (x + t + -5 * y + 2 * t + -3.2 * z).getOrder shouldEqual ExpressionType.LINEAR
    }

    it("x*y + z*t + 5*y + 2*x*t + z*z order should be quadratic") {
      (x * y + z * t + 5 * y + 2 * x * t + z * z).getOrder shouldEqual ExpressionType.QUADRATIC
    }
  }

  describe("Summation and product having many variables") {

    val variables = Array.tabulate(100000)(i => MPFloatVar(i.toString, 0, 1))

    val variables1 = variables.take(5000).map(-_) ++ variables

    val startSum = System.currentTimeMillis()
    sum(variables1)
    info("Summation of " + variables1.length + " variables took " + (System.currentTimeMillis() - startSum) + "ms to calculate")

    val startProd = System.currentTimeMillis()
    val expr = (x + y + x + y + t + z + t + z + 4.1 * y + x + 5) * (x + y + x + y + t + z + t + z + y + x + 2)
    info("Product of " + expr + " took " + (System.currentTimeMillis() - startProd) + "ms to calculate")
    it("Checking product of expressions") {
      expr shouldEqual 12 * x * z + 18.2 * y * t + 4 * z * z + 4 * t * t + 14 * t +
        12 * x * t + 18.2 * y * z + 14 * z + 8 * z * t + 18.299999999999997 * y * y +
        27.2 * y + 27.299999999999997 * x * y + 21 * x + 9 * x * x + 10
    }
  }

  describe("Constraints") {

    val constraint_1 = x * z + 5.7 * x - 34 * t >:= x
    val constraint_2 = x * x + z * t + 9.1 := y * t + 8.7 * z

    it("constraint_1 = x*z + 5.7*x -34*t >= x should be equal to itself") {
      constraint_1.equals(x * z + 5.7 * x - 34 * t >:= x) shouldEqual true
    }

    it("constraint_1 = x*z + 5.7*x -34*t >= x should be equal to x*z + 5.7*x -34*t - x >= 0") {
      constraint_1.equals(x * z + 5.7 * x - 34 * t - x >:= 0) shouldEqual true
    }

    it("constraint_2 = x*x + z*t + 9.1 = y*t + 8.7*z should be equal to itself") {
      constraint_2.equals(x * x + z * t + 9.1 := y * t + 8.7 * z) shouldEqual true
    }

    it("constraint_2 = x*x + z*t + 9.1 = y*t + 8.7*z should be equal to y*t + 8.7*z - x*x - z*t - 9.1 = 0") {
      constraint_2.equals(y * t + 8.7 * z - x * x - z * t - 9.1 := 0) shouldEqual true
    }

    it("constraint_1 should NOT be equal to constraint_2") {
      constraint_1.equals(constraint_2) shouldEqual false
    }
  }
}
