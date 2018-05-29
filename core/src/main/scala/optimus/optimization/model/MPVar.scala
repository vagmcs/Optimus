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

package optimus.optimization.model

import optimus.algebra.{ ANONYMOUS, LongDoubleMap, Var }
import optimus.optimization.MPModel

/**
  * Mathematical programming model variable.
  *
  * @param lowerBound the lower bound in the domain
  * @param upperBound the upper bound in the domain
  * @param symbol the symbol of the variable
  * @param model the model that the variable belongs
  */
class MPVar private[optimization] (
    val lowerBound: Double,
    val upperBound: Double,
    override val symbol: String)
  (implicit model: MPModel) extends Var(symbol) {

  // A variable alone has a coefficient value of 1 in front of her
  val index: Int = model.register(this)
  override val terms = LongDoubleMap(this)

  /**
    * @return the value of the variable (integer rounded if the variable is integer).
    */
  def value: Option[Double] = model.getVarValue(index)

  /**
    * @return the bounds of the variable (lower, upper)
    */
  def bounds: (Double, Double) = (lowerBound, upperBound)

  /**
    * @return true if the variable is integer, false otherwise.
    */
  def isInteger: Boolean = false

  /**
    * @return true if the variable is a binary integer variable (e.g. 0-1).
    */
  def isBinary: Boolean = false

  /**
    * @return true if the variable is unbounded, false otherwise.
    */
  def isUnbounded: Boolean =
    lowerBound == INFINITE && upperBound == INFINITE

  /**
    * @return a textual representation of the variable along its domain
    */
  def toText: String =
    if (isInteger) s"${super.toString} in {${lowerBound.toInt},...,${upperBound.toInt}}"
    else s"${super.toString} in [$lowerBound,$upperBound]"
}

object MPFloatVar {

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @return an unbounded and anonymous float variable
    */
  def apply()(implicit model: MPModel) =
    new MPFloatVar(INFINITE, INFINITE, ANONYMOUS)

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @return an unbounded float variable
    */
  def apply(symbol: String)(implicit model: MPModel) =
    new MPFloatVar(INFINITE, INFINITE, symbol)

  /**
    * Creates a positive variable.
    *
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @return a upper unbounded anonymous float variable
    */
  def positive()(implicit model: MPModel) =
    new MPFloatVar(0, INFINITE, ANONYMOUS)

  /**
    * Creates a positive variable.
    *
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @return a upper unbounded float variable
    */
  def positive(symbol: String)(implicit model: MPModel) =
    new MPFloatVar(0, INFINITE, symbol)

  /**
    * Creates a negative variable.
    *
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @return a upper unbounded anonymous float variable
    */
  def negative()(implicit model: MPModel) =
    new MPFloatVar(INFINITE, 0, ANONYMOUS)

  /**
    * Creates a negative variable.
    *
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @return a upper unbounded float variable
    */
  def negative(symbol: String)(implicit model: MPModel) =
    new MPFloatVar(INFINITE, 0, symbol)

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param lowerBound the lower bound in the domain
    * @param upperBound the upper bound in the domain
    * @return a bounded anonymous float variable
    */
  def apply(lowerBound: Double, upperBound: Double)(implicit model: MPModel) =
    new MPFloatVar(lowerBound, upperBound, ANONYMOUS)

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @param lowerBound the lower bound of variable domain
    * @param upperBound the upper bound of variable domain
    * @return a bounded float variable
    */
  def apply(symbol: String, lowerBound: Double, upperBound: Double)(implicit model: MPModel) =
    new MPFloatVar(lowerBound, upperBound, symbol)
}

class MPIntVar private[optimization] (lowerBound: Int, upperBound: Int, override val symbol: String)
  (implicit model: MPModel) extends MPVar(lowerBound, upperBound, symbol)(model) {
  override def isInteger: Boolean = true
}

class MPBinaryVar private[optimization] (override val symbol: String)
  (implicit model: MPModel) extends MPIntVar(0, 1, symbol)(model) {
  override def isBinary: Boolean = true
}

object MPIntVar {

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param domain the variable domain defined as a range of integers. If the
    *               range is 0 to 1 then the variable is binary.
    * @return an anonymous integer variable
    */
  def apply(domain: Range)(implicit model: MPModel): MPIntVar =
    if (domain.start == 0 && domain.end == 1) MPBinaryVar()
    else new MPIntVar(domain.start, domain.max, ANONYMOUS)

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @param domain the variable domain defined as a range of integers. If the
    *               range is 0 to 1 then the variable is binary.
    * @return an integer variable
    */
  def apply(symbol: String, domain: Range)(implicit model: MPModel): MPIntVar =
    if (domain.start == 0 && domain.end == 1) MPBinaryVar(symbol)
    else new MPIntVar(domain.start, domain.max, symbol)
}

object MPBinaryVar {

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @return an anonymous binary variable
    */
  def apply()(implicit model: MPModel): MPBinaryVar =
    new MPBinaryVar(ANONYMOUS)

  /**
    * @see [[optimus.optimization.model.MPVar]]
    *
    * @param symbol the symbol of the variable
    * @return a binary variable
    */
  def apply(symbol: String)(implicit model: MPModel): MPBinaryVar =
    new MPBinaryVar(symbol)
}
