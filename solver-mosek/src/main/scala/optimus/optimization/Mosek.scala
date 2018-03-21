package optimus.optimization

import mosek._
import optimus.algebra._
import optimus.optimization.PreSolve.PreSolve
import optimus.optimization.ProblemStatus.ProblemStatus

import scala.util.{Failure, Success, Try}

final class Mosek extends AbstractMPSolver {

  var tempRow = 0
  var nbRows = 0
  var nbCols = 0
  var solution = Array[Double]()
  var objectiveValue = 0.0
  var status = ProblemStatus.NOT_SOLVED

  val env = new Env()
  val task = new Task(env)

  /**
    * Problem builder, should configure the solver and append
    * mathematical model variables.
    *
    * @param nbRows rows in the model
    * @param nbCols number of variables in the model
    */
  override def buildProblem(nbRows: Int, nbCols: Int) = {

    logger.info {
      """     __  ___                __   """ + "\n" +
      """    /  |/  /___  ________  / /__ """ + "\n" +
      """   / /|_/ / __ \/ ___/ _ \/ //_/ """ + "\n" +
      """  / /  / / /_/ (__  )  __/ ,<    """ + "\n" +
      """ /_/  /_/\____/____/\___/_/|_|   """ + "\n"
    }

    logger.info("Model mosek: " + nbRows + "x" + nbCols)

    this.nbRows = 0
    this.nbCols = nbCols

    solution = new Array[Double](nbCols)

    task.appendcons(nbRows)
    task.appendvars(nbCols)

    val cols = (0 until nbCols).toArray

    task.putvarnamelist(cols, cols map ("x" + _))
    task.putvartypelist(cols, cols map (i => variabletype.type_cont))
    task.putvarboundlist(cols, cols map (i => boundkey.up), cols map (i => 0.0), cols map (i => Double.MaxValue))
  }

  /**
    * Get value of the variable in the specified position. Solution
    * should exist in order for a value to exist.
    *
    * @param colId position of the variable
    * @return the value of the variable in the solution
    */
  override def getValue(colId: Int): Double = solution(colId)

  /**
    * Set upper bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  override def setUnboundUpperBound(colId: Int) = {
    task.putvarbound(colId, boundkey.fr, Double.MinValue, Double.MaxValue)

  }

  /**
    * Set lower bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  override def setUnboundLowerBound(colId: Int): Unit = ???

  /**
    * Solve the problem.
    *
    * @param preSolve pre-solving mode
    * @return status code indicating the nature of the solution
    */
  override def solveProblem(preSolve: PreSolve): ProblemStatus = {

    val optimizationStatus = task.optimize()

    optimizationStatus match {
      case rescode.ok =>
        val solutionStatus = new Array[solsta](1)
        task.getsolsta(soltype.itr, solutionStatus)

        solutionStatus.head match {
          case solsta.optimal =>
            task.getxx(soltype.itr, solution)
            objectiveValue = task.getprimalobj(soltype.itr)
            ProblemStatus.OPTIMAL

          case solsta.near_optimal =>
            task.getxx(soltype.itr, solution)
            objectiveValue = task.getprimalobj(soltype.itr)
            ProblemStatus.SUBOPTIMAL

          case solsta.dual_infeas_cer |
               solsta.prim_infeas_cer |
               solsta.near_dual_infeas_cer |
               solsta.near_prim_infeas_cer =>
            ProblemStatus.INFEASIBLE

          case _ =>
            ProblemStatus.NOT_SOLVED
        }
      case _ =>
        logger.info(s"Optimization failed with rescode = $optimizationStatus")
        ProblemStatus.NOT_SOLVED
    }

  }

  /**
    * Add objective expression to be optimized by the solver.
    *
    * @param objective the expression to be optimized
    * @param minimize  flag for minimization instead of maximization
    */
  override def addObjective(objective: Expression, minimize: Boolean) = {

    objective.getOrder match {
      case ExpressionOrder.HIGHER => throw new IllegalArgumentException("Higher than quadratic: " + objective)

      case ExpressionOrder.QUADRATIC =>
        val iterator = objective.terms.iterator
        while(iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if(indexes.length == 1)
            task.putcj(indexes.head, iterator.value)
          else task.putqobjij(Math.max(indexes.head, indexes(1)), Math.min(indexes.head, indexes(1)), iterator.value)
        }
      case ExpressionOrder.LINEAR =>
        val variables = objective.terms.keys.map(code => decode(code).head)
        task.putclist(variables, objective.terms.values)

      case ExpressionOrder.CONSTANT =>
        task.putcfix(objective.constant)
    }

    task.putobjsense(if (minimize) objsense.minimize else objsense.maximize)
  }

  /**
    * Add a mathematical programming constraint to the solver.
    *
    * @param mpConstraint the mathematical programming constraint
    */
  override def addConstraint(mpConstraint: MPConstraint) = {

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs
    val rhs = -lhs.constant
    val operator = mpConstraint.constraint.operator

    lhs.getOrder match {
      case ExpressionOrder.HIGHER => throw new IllegalArgumentException("Higher than quadratic: " + lhs)

      case ExpressionOrder.QUADRATIC =>
        var linearIndexes = Array.emptyIntArray
        var linearValues = Array.emptyDoubleArray
        var rowIndexes = Array.emptyIntArray
        var colsIndexes = Array.emptyIntArray
        var quadraticValues = Array.emptyDoubleArray

        val iterator = lhs.terms.iterator
        while(iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if(indexes.length == 1) {
            linearIndexes :+= indexes.head
            linearValues :+= iterator.value
          }
          else {
            rowIndexes :+= Math.max(indexes.head, indexes(1))
            colsIndexes :+= Math.min(indexes.head, indexes(1))
            quadraticValues :+= iterator.value()
          }
        }
        task.putarow(nbRows, linearIndexes, linearValues)
        task.putqconk(nbRows, rowIndexes, colsIndexes, quadraticValues)

      case ExpressionOrder.LINEAR =>
        val variables = lhs.terms.keys.map(code => decode(code).head)
        task.putarow(nbRows, variables, lhs.terms.values)
    }

    operator match {
      case ConstraintRelation.GE => task.putconbound(nbRows, boundkey.lo, rhs, Double.MaxValue)
      case ConstraintRelation.LE => task.putconbound(nbRows, boundkey.up, Double.MinValue, rhs)
      case ConstraintRelation.EQ => task.putconbound(nbRows, boundkey.fx, rhs, rhs)
    }

    nbRows += 1
  }

  /**
    * Set the column/variable as a float variable
    *
    * @param colId position of the variable
    */
  override def setFloat(colId: Int): Unit = task.putvartype(colId, variabletype.type_cont)

  /**
    * Set bounds of variable in the specified position.
    *
    * @param colId position of the variable
    * @param lower domain lower bound
    * @param upper domain upper bound
    */
  override def setBounds(colId: Int, lower: Double, upper: Double): Unit =
    task.putvarbound(colId, boundkey.ra, lower, upper)

  /**
    * Set a time limit for solver optimization. After the limit
    * is reached the solver stops running.
    *
    * @param limit the time limit
    */
  override def setTimeout(limit: Int): Unit = {
    task.putdouparam(dparam.optimizer_max_time, limit)
  }

  /**
    * Set the column/variable as an integer variable
    *
    * @param colId position of the variable
    */
  override def setInteger(colId: Int) {
    task.putvartype(colId, variabletype.type_int)
  }

  /**
    * Release the memory of this solver
    */
  override def release() {
    task.dispose()
    env.dispose()
  }

  /**
    * Set the column / variable as an binary integer variable
    *
    * @param colId position of the variable
    */
  override def setBinary(colId: Int): Unit = {
    task.putvartype(colId, variabletype.type_int)
  }
}
