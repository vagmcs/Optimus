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

import com.typesafe.scalalogging.LazyLogging
import optimus.algebra.ConstraintRelation._

/**
 * Expression abstraction, should be extended by anything that is
 * an expression type.
 */
abstract class Expression extends LazyLogging {

  /*
   * Store the variables, the corresponding scalars and
   * the constant term of the expression.
   */
  val constant: Double = 0.0
  val terms: LongDoubleMap = LongDoubleMap.empty

  def +(that: Expression): Expression = that match {
    case Zero => this
    case _ => Plus(this, that)
  }

  def -(that: Expression): Expression = that match {
    case Zero => this
    case _ => Minus(this, that)
  }

  def *(that: Expression): Expression = that match {
    case Zero => Zero
    case One => this
    case _ => Product(this, that)
  }

  def unary_- : Expression = Minus(0, this)

  def <:=(that: Expression): Constraint = Constraint(this, LE, that)

  def >:=(that: Expression): Constraint = Constraint(this, GE, that)

  def :=(that: Expression): Constraint = Constraint(this, EQ, that)

  // Order of the expression (e.g linear) maybe is a little slow
  @inline private def order: Int = {
    var order = 0
    val iterator = terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      order = Math.max(order, decode(iterator.key).length)
    }
    order
  }

  def getOrder: ExpressionType = order match {
    case 0 => ExpressionType.CONSTANT
    case 1 => ExpressionType.LINEAR
    case 2 => ExpressionType.QUADRATIC
    case _ => ExpressionType.GENERIC
  }

  override def toString: String = (terms.keys zip terms.values)
    .map { case (i, scalar) => s"$scalar*${decode(i).map(x => s"x@$x").mkString("*")}" }
    .mkString(" + ") + (if (constant != 0) " + " + constant else "")

  /**
   * @param obj an object to compare
   * @return true in case this object has identical constant
   *         and terms as the obj argument; false otherwise.
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: Expression => this.constant == that.constant && this.terms == that.terms
    case _ => false
  }
}

/**
 * Abstract variable, should be extended by any variable type in order
 * to inherit the algebraic properties.
 *
 * @param symbol the symbol of the variable
 */
abstract class Var(val symbol: String) extends Expression {

  val upperBound: Double
  val lowerBound: Double
  val index: Int

  override def *(other: Expression): Expression = other match {

    case Zero => Zero

    case c: Const => Term(c, Vector(this))

    case v: Var => Term(One, Vector(this, v))

    case t: Term => Term(t.scalar, t.vars :+ this)

    case _ => Product(this, other)
  }

  override def unary_- : Expression = Term(Const(-1), Vector(this))

  /** @return the symbol of the variable */
  override def toString: String = if (symbol != ANONYMOUS) symbol else s"x@$index"

  /** @return the index of the variable */
  override def hashCode: Int = index

  /**
   * @param obj an object to compare
   * @return true only in case the object is a variable
   *         and has identical index
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: Var => this.index == that.index
    case _ => false
  }
}

/**
 * Term is holding a coefficient and all variables which are involved
 * in the product of the term.
 *
 * scalar * (var_1 * ... * var_n)
 */
case class Term(scalar: Const, vars: Vector[Var]) extends Expression {

  require(vars.length < 3, throw new UnsupportedOperationException("Only up to quadratic expressions are supported!"))

  override val terms: LongDoubleMap = LongDoubleMap(scalar, vars)

  override def *(that: Expression): Expression = that match {

    case Zero => Zero

    case One => this

    case c: Const => Term(scalar * c, vars)

    case v: Var => Term(scalar, vars :+ v)

    case t: Term => Term(scalar * t.scalar, vars ++ t.vars)

    case _ => Product(this, that)
  }

  override def unary_- : Expression = Term(Const(-scalar.value), vars)

  override def toString: String = s"$scalar${vars.mkString("*")}"
}

/**
 * Constant expression holding a double value.
 *
 * @param value the value held by the constraint
 */
class Const(val value: Double) extends Expression {

  override val constant: Double = value

  def +(other: Const): Const = other match {
    case Zero => this
    case _ => Const(value + other.value)
  }

  def -(other: Const): Const = other match {
    case Zero => this
    case _ => Const(value - other.value)
  }

  def *(other: Const): Const = other match {
    case Zero => Zero
    case One => this
    case _ => Const(value * other.value)
  }

  def *(x: Var): Term = Term(this, Vector(x))

  def *(term: Term): Term = Term(this * term.scalar, term.vars)

  override def *(other: Expression): Expression = ConstProduct(this, other)

  override def unary_- : Expression = Const(-value)

  override def toString: String = value.toString

  /** @return the hash code of the boxed double value */
  override def hashCode: Int = value.##

  /**
   * @param obj an object to compare
   * @return true only in case the object is a constant
   *         and has identical value
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: Const => this.value == that.value
    case _ => false
  }
}

object Const {
  def apply(value: Double): Const = value match {
    case 0 => Zero
    case 1 => One
    case _ => new Const(value)
  }
}

case object Zero extends Const(0) {

  override def +(expression: Expression): Expression = expression

  override def -(expression: Expression): Expression = -expression

  override def *(expression: Expression): Expression = this

  override def unary_- : Expression = this
}

case object One extends Const(1) {

  override def *(expression: Expression): Expression = expression

  override def unary_- : Const = Const(-1)
}

/**
 * Const product represents an expression multiplied by a constant and has
 * the form of (c * a).
 *
 * @param scalar the constant
 * @param a the expression
 */
case class ConstProduct(scalar: Const, a: Expression) extends Expression {

  override val constant: Double = scalar.value * a.constant

  override val terms: LongDoubleMap = scalar match {
    case Zero => LongDoubleMap.empty
    case _ => LongDoubleMap(a.terms.keys, a.terms.values.map(_ * scalar.value))
  }

  override def unary_- : Expression = ConstProduct(Const(-scalar.value), a)
}

// ------------------------------------- Operator Expressions -------------------------------------

/**
 * Binary operator expression (a operator b), that should be extended
 * by any binary operator expression type.
 *
 * @param a left hand side expression
 * @param b right hand side expression
 */
abstract class BinaryOp(val a: Expression, val b: Expression) extends Expression {

  override val constant: Double = op(a.constant, b.constant)

  override val terms: LongDoubleMap = merge

  protected def op(x: Double, y: Double): Double

  protected def merge: LongDoubleMap = {

    // 1. Add all terms of expression A to the result
    val result = LongDoubleMap(a.terms)

    // 2. For each term of the expression B apply the operation
    val iterator = b.terms.iterator
    while (iterator.hasNext) {
      iterator.advance()
      val value = op(0, iterator.value)
      result.adjustOrPutValue(iterator.key, value, value)
    }

    // 3. Filter out zero terms
    result.retainEntries((_: UniqueId, v: Double) => v != 0d)
    result
  }
}

/**
 * Plus operator for addition (a + b).
 *
 * @param a left hand side expression
 * @param b right hand side expression
 */
case class Plus(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  protected def op(x: Double, y: Double): Double = x + y

  override def unary_- : Expression = Plus(-a, -b)
}

/**
 * Minus operator for subtraction (a - b).
 *
 * @param a left hand side expression
 * @param b right hand side expression
 */
case class Minus(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  protected def op(x: Double, y: Double): Double = x - y

  override def unary_- : Expression = Minus(b, a)
}

/**
 * Product operator (a * b).
 *
 * @param a left hand side expression
 * @param b right hand side expression
 */
case class Product(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  protected def op(x: Double, y: Double): Double = x * y

  override protected def merge: LongDoubleMap = {

    var isTraversed = false
    val result = LongDoubleMap.empty

    val iteratorA = a.terms.iterator
    while (iteratorA.hasNext) {
      iteratorA.advance()
      val variablesA = iteratorA.key
      val cA = iteratorA.value

      // 1. Calculate products involving terms only from expression A and the constant of B
      result.adjustOrPutValue(variablesA, cA * b.constant, cA * b.constant)

      val iteratorB = b.terms.iterator
      while (iteratorB.hasNext) {
        iteratorB.advance()
        val variablesB = iteratorB.key
        val cB = iteratorB.value

        // 2. Calculate products involving terms from both A and B expressions
        val variablesProduct = decode(variablesA) ++ decode(variablesB)
        assert(variablesProduct.length <= 2, "Algebra cannot handle expressions of higher order!")

        val variables = encode(variablesProduct.head, variablesProduct.last)
        result.adjustOrPutValue(variables, cA * cB, cA * cB)

        // 3. Calculate products involving terms only from expression B and the constant of A
        if (!isTraversed) result.adjustOrPutValue(variablesB, cB * a.constant, cB * a.constant)
      }

      isTraversed = true
    }

    // 4. Filter out zero terms
    result.retainEntries((_: UniqueId, v: Double) => v != 0d)
    result
  }
}
