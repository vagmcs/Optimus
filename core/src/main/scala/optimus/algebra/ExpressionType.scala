package optimus.algebra

sealed trait ExpressionType

object ExpressionType {

  case object CONSTANT extends ExpressionType
  case object LINEAR extends ExpressionType
  case object QUADRATIC extends ExpressionType
  case object GENERIC extends ExpressionType
}
