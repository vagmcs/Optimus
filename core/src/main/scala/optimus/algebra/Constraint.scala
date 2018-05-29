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

/**
  * A constraint has the form (expression RELATION expression). RELATION can be
  * one of the {<=, =, >=}.
  *
  * @param lhs left hand side expression
  * @param operator relation operator
  * @param rhs right hand side expression
  */
case class Constraint(lhs: Expression, operator: ConstraintRelation, rhs: Expression) {

  override def toString: String = s"$lhs $operator $rhs"

  /**
    * @param obj an object to compare
    * @return true in case this object has identical constant
    *         and terms as the obj argument; false otherwise.
    */
  override def equals(obj: Any): Boolean = obj match {
    case that: Constraint =>

      // Move terms in the left hand side of the expression in order to properly check equality.
      val (a, b) = (lhs - rhs, that.lhs - that.rhs)

      operator == that.operator && (a == b || -a == b || a == -b || -a == -b)
    case _ => false
  }
}
