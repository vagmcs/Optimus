package optimus.optimization

import optimus.algebra.{ANONYMOUS, LongDoubleMap, Var}

/**
  * Mathematical programming unbounded variable in the problem. The domain is defined
  * (0, +inf) if the variable is unbounded or (-inf, +inf) otherwise.
  *
  * @param problem the problem in which the variable belongs
  * @param lowerBound the lower bound in the domain
  * @param upperBound the upper bound in the domain
  * @param doubleUnbounded unbounded domain (-inf, +inf)
  * @param symbol the symbol of the variable (default is ANONYMOUS)
  *
  */
class MPVariable(val problem: AbstractMPProblem, val lowerBound: Double, val upperBound: Double, doubleUnbounded: Boolean,
                 override val symbol: String = ANONYMOUS) extends Var(symbol) {

  val index = problem.register(this)

  // A variable alone has a coefficient value of 1 in front of her
  override val terms = LongDoubleMap(this)

  protected var integer = false

  protected var binary = false

  protected var unbounded = doubleUnbounded

  /**
    * @return the value of the variable (integer rounded if the variable is integer).
    */
  def value = problem.getValue(index)

  /**
    * @return true if the variable is integer, false otherwise.
    */
  def isInteger = integer

  /**
    * @return true if the variable is a binary integer variable (e.g. 0-1).
    */
  def isBinary = binary

  /**
    * @return true if the variable is unbounded, false otherwise.
    */
  def isUnbounded = unbounded
}
