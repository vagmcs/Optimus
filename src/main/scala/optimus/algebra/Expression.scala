package optimus.algebra

import optimus.algebra.ExpressionOrder.ExpressionOrder

/*
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
 * Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 * Optimus is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Optimus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

/**
 * Expression abstraction, should be extended by anything that is
 * an expression type.
 *
 * @author Vagelis Michelioudakis
 * @author Anastasios Skarlatidis
 */
abstract class Expression {

  // keep the variables and their corresponding coefficients and the constant term of the expression
  val constant: Double
  val terms: Map[Vector[Variable], Double]

  def +(other: Expression): Expression = other match {
    case Zero => this
    case _ => Plus(this, other)
  }

  def -(other: Expression): Expression = other match {
    case Zero => this
    case _ => Minus(this, other)
  }

  def *(other: Expression): Expression = other match {
    case Zero => Zero
    case One => this
    case _ => Product(this, other)
  }

  def unary_-(): Expression = Minus(0, this)

  def <=(other: Expression) = new Constraint(this, ConstraintRelation.LE, other)

  def >=(other: Expression) = new Constraint(this, ConstraintRelation.GE, other)

  def :=(other: Expression) = new Constraint(this, ConstraintRelation.EQ, other)

  private def order = {
    var order = 0
    for(term <- terms) order = Math.max(order, term._1.length)
    order
  }

  def getOrder: ExpressionOrder = order match {
    case 0 => ExpressionOrder.CONSTANT
    case 1 => ExpressionOrder.LINEAR
    case 2 => ExpressionOrder.QUADRATIC
    case _ => ExpressionOrder.HIGHER
  }

  override def toString =
    if (terms.isEmpty) constant.toString
    else "(" + terms.map(pair => pair._2 + pair._1.mkString("*")).reduce(_ + " + " + _) + " + " + constant + ")"

  override def equals(that: Any) = that match {
    case other: Expression =>
      terms == other.terms
    case _ => false
  }
}

/**
 * Abstract variable, should be extended by any variable type in order
 * to inherit the algebraic properties.
 *
 * @param symbol the symbol of the variable
 *
 * @author Vagelis Michelioudakis
 * @author Anastasios Skarlatidis
 */
abstract class Variable(val symbol: String) extends Expression with Ordered[Variable] {

  // A variable alone has a constant value of 1 in front of her
  val constant = 0.0
  val terms = Map(Vector(this) -> 1.0)

  val upperBound: Double
  val lowerBound: Double
  val index: Int

  override def *(other: Expression) = other match {

    case Zero => Zero

    case c: Const => Term(c, Vector(this))

    case variable: Variable => Term(One, Vector(this, variable))

    case term: Term => Term(term.coefficient, term.variables :+ this)

    case _ => Product(this, other)
  }

  override def unary_-(): Expression = Term(Const(-1.0), Vector(this))

  def ^(power: Int): Expression = {
    require(power >= 0, "Power should be positive!")
    power match {
      case 0 => One
      case _ => Term(One, Vector.fill(power)(this))
    }
  }
  
  def compare(that: Variable) = index - that.index

  override def toString = symbol

  override def equals(that: Any) = that match {
    case other: Variable => index == other.index
    case _ => false
  }
}

/**
 * Object holding the anonymous constant for variables not having a
 * specified symbol.
 */
object Variable { final val ANONYMOUS = "" }

/**
 * Term is holding a coefficient and all variables which are involved
 * in the product of the term.
 *
 * coefficient * (variable_1 * ... * variable_n)
 *
 * @author Vagelis Michelioudakis
 * @author Anastasios Skarlatidis
 */
case class Term(coefficient: Const, variables: Vector[Variable]) extends Expression {

  val constant = 0.0
  val terms = Map(variables.sorted -> coefficient.value)

  override def *(other: Expression) = other match {

    case Zero => Zero

    case One => this

    case c: Const => Term(Const(coefficient.value * c.value), variables)

    case v: Variable => Term(coefficient, variables :+ v)

    case term: Term => Term(coefficient * term.coefficient, variables ++ term.variables)

    case _ => Product(this, other)
  }

  override def unary_-(): Expression = Term(Const(-coefficient.value), variables)

  override def toString = coefficient + variables.mkString("*")
}

/**
 * Constant expression holding a double value.
 *
 * @param value the value held by the constraint
 */
class Const(val value: Double) extends Expression {

  val constant = value
  val terms = Map.empty[Vector[Variable], Double]

  def +(other: Const) = other match {
    case Zero => this
    case _ => Const(value + other.value)
  }

  def -(other: Const) = other match {
    case Zero => this
    case _ => Const(value - other.value)
  }

  def *(other: Const) = other match {
    case Zero => Zero
    case One => this
    case _ => Const(value * other.value)
  }

  def *(x: Variable) = Term(this, Vector(x))

  def *(term: Term): Term = Term(this * term.coefficient, term.variables)

  override def *(other: Expression): Expression = ConstProduct(this, other)

  override def unary_-(): Expression = Const(-value)

  override def toString = value.toString

  override def equals(that: Any) = that match {
    case other: Const => value == other.value
    case _ => false
  }
}

object Const {

  def apply(value: Double) = value match {
    case 0.0 => Zero
    case 1.0 => One
    case _ => new Const(value)
  }
}

/**
 * Zero is representing the special case of Zero constant.
 */
case object Zero extends Const(0.0) {

  override def +(expression: Expression) = expression

  override def -(expression: Expression) = -expression

  override def unary_-(): Expression = this

  override def *(expression: Expression) = this
}

/**
 * One is representing the special case of One constant.
 */
case object One extends Const(1.0) {

  override def *(expression: Expression) = this

  override def unary_-() = Const(-1.0)
}

/**
 * Const product represents an expression multiplied by a constant and has
 * the form of (c * a).
 *
 * @param c the constant
 * @param a the expression
 */
case class ConstProduct(c: Const, a: Expression) extends Expression {

  val constant = if (c == Zero) 0.0 else c.value * a.constant

  val terms = if (c == Zero) Map.empty[Vector[Variable], Double]
              else a.terms.map(pair => pair._1 -> c.value * pair._2)

  override def unary_-(): Expression = ConstProduct(Const(-c.value), a)
}

// ------------------------------------- Operator Expressions -------------------------------------

/**
 * Binary operator abstraction of the form (a operator b), that should be extended by any binary
 * operator expression type.
 *
 * @param a the left hand side expression
 * @param b the right had side expression
 */
abstract class BinaryOp(val a: Expression, val b: Expression) extends Expression {

  val constant = op(a.constant, b.constant)
  val terms = merge

  def op(x: Double, y: Double): Double

  def merge: Map[Vector[Variable], Double] = {
    var temporal = scala.collection.mutable.Map[Vector[Variable], Double]()

    // 1. Add all terms of expression A to the result
    for ( (variables, c) <- a.terms )
      temporal += variables -> c

    // 2. For each term of the expression B perform the desired operation if the term already exists
    for ( (variables, c) <- b.terms ) {
      temporal.get(variables) match {
        case Some(coefficient) => temporal(variables) = op(coefficient, c)
        case None => temporal += (variables -> op(0, c))
      }
    }

    // 3. Filter out zero terms
    temporal.filterNot(_._2 == 0).toMap
  }
}

/**
 * Binary operator for addition has the form (a + b).
 *
 * @param a the left hand side expression
 * @param b the right had side expression
 */
case class Plus(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  def op(x: Double, y: Double) = x + y

  override def unary_-(): Expression = Plus(-a, -b)
}

/**
 * Binary operator for subtraction has the form (a - b).
 *
 * @param a the left hand side expression
 * @param b the right had side expression
 */
case class Minus(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  def op(x: Double, y: Double) = x - y

  override def unary_-(): Expression = Minus(b, a)
}

/**
 * Binary operator for multiplication has the form (a * b).
 *
 * @param a the left hand side expression
 * @param b the right had side expression
 */
case class Product(override val a: Expression, override val b: Expression) extends BinaryOp(a, b) {

  def op(x: Double, y: Double) = x * y

  override def merge: Map[Vector[Variable], Double] = {
    var temporal = scala.collection.mutable.Map[Vector[Variable], Double]()

    for ( (variablesA, cA) <- a.terms) {

      // 1. Calculate products involving terms only from expression A and the constant of B
      temporal.get(variablesA) match {
        case None => temporal += (variablesA -> cA*b.constant)
        case Some(c) => temporal(variablesA) = c + cA*b.constant
      }

      for( (variablesB, cB) <- b.terms) {

        // 2. Calculate products involving terms from both A and B expressions
        val variables = (variablesA ++ variablesB).sorted

        temporal.get(variables) match {
          case None => temporal += (variables -> cA*cB)
          case Some(c) => temporal(variables) = c + cA*cB
        }

        // 3. Calculate products involving terms only from expression B and the constant of A
        temporal.get(variablesB) match {
          case None => temporal += (variablesB -> cB*a.constant)
          case Some(c) => temporal(variablesB) = c + cB*a.constant
        }

      }
    }

    // 4. Filter out zero terms
    temporal.filterNot(_._2 == 0).toMap
  }
}
