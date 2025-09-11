package optimus

import optimus.algebra.{ Constraint, Expression }
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

package object optimization {

  def add(constraint: Constraint)(implicit model: MPModel): MPConstraint = model.add(constraint)

  def subjectTo(constraints: Constraint*)(implicit model: MPModel): Unit = constraints.foreach(add)

  def start(
    preSolve: PreSolve = PreSolve.DISABLED,
    timeLimit: Int = Int.MaxValue
  )(implicit
    model: MPModel
  ): Boolean = model.start(timeLimit, preSolve)

  def minimize(expression: Expression)(implicit model: MPModel): MPModel = model.minimize(expression)

  def maximize(expression: Expression)(implicit model: MPModel): MPModel = model.maximize(expression)

  def release()(implicit model: MPModel): Unit = model.release()

  def objectiveValue(implicit model: MPModel): Double = model.objectiveValue

  def status(implicit model: MPModel): SolutionStatus = model.getStatus

  def checkConstraints(tol: Double = 10e-6)(implicit model: MPModel): Boolean = model.checkConstraints(tol)
}
