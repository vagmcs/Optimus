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

package optimus.optimization

import gurobi._
import optimus.algebra._
import optimus.optimization.enums.PreSolve._
import optimus.optimization.enums.{ PreSolve, SolutionStatus }
import optimus.optimization.model.MPConstraint

/**
  * Gurobi solver.
  */
final class Gurobi extends MPSolver {

  type Solver = GRBModel

  protected var underlyingSolver: Solver = new GRBModel(new GRBEnv())

  /**
    * Problem builder, should configure the solver and append
    * mathematical model variables and constraints.
    *
    * @param numberOfVars number of variables in the model
    */
  def buildModel(numberOfVars: Int): Unit = {

    logger.info {
      "\n" +
        """    _________                   ______ _____  """ + "\n" +
        """    __  ____/___  _________________  /____(_) """ + "\n" +
        """    _  / __ _  / / /_  ___/  __ \_  __ \_  /  """ + "\n" +
        """    / /_/ / / /_/ /_  /   / /_/ /  /_/ /  /   """ + "\n" +
        """    \____/  \__._/ /_/    \____//_____//_/    """ + "\n"
    }

    this.numberOfVars = numberOfVars

    underlyingSolver.getEnv.set(GRB.IntParam.OutputFlag, 0)

    underlyingSolver.addVars(
      Array.fill(numberOfVariables)(0),
      Array.fill(numberOfVariables)(GRB.INFINITY),
      Array.fill(numberOfVariables)(0),
      Array.fill(numberOfVariables)(GRB.CONTINUOUS),
      Array.tabulate(numberOfVariables)(i => s"x$i")
    )

    underlyingSolver.update()
  }

  /**
    * Get value of the variable in the specified position. Solution
    * should exist in order for a value to exist.
    *
    * @param colId position of the variable
    * @return the value of the variable in the solution
    */
  def getVarValue(colId: Int): Double = solution(colId)

  /**
    * Set bounds of variable in the specified position.
    *
    * @param colId position of the variable
    * @param lower domain lower bound
    * @param upper domain upper bound
    */
  def setBounds(colId: Int, lower: Double, upper: Double): Unit = {
    val GRBVar = underlyingSolver.getVar(colId)
    GRBVar.set(GRB.DoubleAttr.LB, lower)
    GRBVar.set(GRB.DoubleAttr.UB, upper)
  }

  /**
    * Set upper bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundUpperBound(colId: Int): Unit = {
    underlyingSolver.getVar(colId).set(GRB.DoubleAttr.UB, GRB.INFINITY)
  }

  /**
    * Set lower bound to unbounded (infinite)
    *
    * @param colId position of the variable
    */
  def setUnboundLowerBound(colId: Int): Unit = {
    underlyingSolver.getVar(colId).set(GRB.DoubleAttr.LB, -GRB.INFINITY)
  }

  /**
    * Set the column/variable as an integer variable
    *
    * @param colId position of the variable
    */
  def setInteger(colId: Int): Unit = {
    underlyingSolver.getVar(colId).set(GRB.CharAttr.VType, GRB.INTEGER)
  }

  /**
    * Set the column / variable as an binary integer variable
    *
    * @param colId position of the variable
    */
  def setBinary(colId: Int): Unit = {
    underlyingSolver.getVar(colId).set(GRB.CharAttr.VType, GRB.BINARY)
  }

  /**
    * Set the column/variable as a float variable
    *
    * @param colId position of the variable
    */
  def setFloat(colId: Int): Unit = {
    underlyingSolver.getVar(colId).set(GRB.CharAttr.VType, GRB.CONTINUOUS)
  }

  /**
    * Add objective expression to be optimized by the solver.
    *
    * @param objective the expression to be optimized
    * @param minimize flag for minimization instead of maximization
    */
  def setObjective(objective: Expression, minimize: Boolean): Unit = {

    objective.getOrder match {
      case ExpressionType.GENERIC => throw new IllegalArgumentException("Higher than quadratic: " + objective)

      case ExpressionType.QUADRATIC =>
        val QExpression = new GRBQuadExpr
        val iterator = objective.terms.iterator
        while (iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if (indexes.length == 1) QExpression.addTerm(iterator.value, underlyingSolver.getVar(indexes.head))
          else QExpression.addTerm(iterator.value, underlyingSolver.getVar(indexes.head), underlyingSolver.getVar(indexes(1)))
        }
        QExpression.addConstant(objective.constant)
        underlyingSolver.setObjective(QExpression, if (minimize) 1 else -1)

      case ExpressionType.LINEAR =>
        val LExpression = new GRBLinExpr
        val variables = objective.terms.keys.map(code => underlyingSolver.getVar(decode(code).head))
        LExpression.addTerms(objective.terms.values, variables)
        LExpression.addConstant(objective.constant)
        underlyingSolver.setObjective(LExpression, if (minimize) 1 else -1)

      case ExpressionType.CONSTANT =>
        val CExpression = new GRBLinExpr
        CExpression.addConstant(objective.constant)
        underlyingSolver.setObjective(CExpression, if (minimize) 1 else -1)
    }

    underlyingSolver.update()
  }

  /**
    * Add a mathematical programming constraint to the solver.
    *
    * @param mpConstraint the mathematical programming constraint
    */
  def addConstraint(mpConstraint: MPConstraint): Unit = {

    numberOfCons += 1

    val lhs = mpConstraint.constraint.lhs - mpConstraint.constraint.rhs
    val rhs = -lhs.constant
    val operator = mpConstraint.constraint.operator

    val GRBOperator = operator match {
      case ConstraintRelation.GE => GRB.GREATER_EQUAL
      case ConstraintRelation.LE => GRB.LESS_EQUAL
      case ConstraintRelation.EQ => GRB.EQUAL
    }

    lhs.getOrder match {
      case ExpressionType.GENERIC => throw new IllegalArgumentException("Higher than quadratic: " + lhs)

      case ExpressionType.QUADRATIC =>
        val QExpression = new GRBQuadExpr
        val iterator = lhs.terms.iterator
        while (iterator.hasNext) {
          iterator.advance()
          val indexes = decode(iterator.key)
          if (indexes.length == 1) QExpression.addTerm(iterator.value, underlyingSolver.getVar(indexes.head))
          else QExpression.addTerm(iterator.value, underlyingSolver.getVar(indexes.head), underlyingSolver.getVar(indexes(1)))
        }
        underlyingSolver.addQConstr(QExpression, GRBOperator, rhs, "")

      case ExpressionType.LINEAR | ExpressionType.CONSTANT =>
        val LExpression = new GRBLinExpr
        val iterator = lhs.terms.iterator
        while (iterator.hasNext) {
          iterator.advance()
          LExpression.addTerm(iterator.value, underlyingSolver.getVar(decode(iterator.key).head))
        }
        underlyingSolver.addConstr(LExpression, GRBOperator, rhs, "")
    }
  }

  /**
    * Solve the problem.
    *
    * @return status code indicating the nature of the solution
    */
  def solve(preSolve: PreSolve = DISABLED): SolutionStatus = {

    if (preSolve == CONSERVATIVE) underlyingSolver.getEnv.set(GRB.IntParam.Presolve, 1)
    else if (preSolve == AGGRESSIVE) underlyingSolver.getEnv.set(GRB.IntParam.Presolve, 2)

    underlyingSolver.update()
    underlyingSolver.optimize()

    var optimizationStatus = underlyingSolver.get(GRB.IntAttr.Status)

    _solutionStatus = if (optimizationStatus == GRB.INF_OR_UNBD) {
      underlyingSolver.getEnv.set(GRB.IntParam.Presolve, 0)
      underlyingSolver.optimize()
      optimizationStatus = underlyingSolver.get(GRB.IntAttr.Status)
      SolutionStatus.UNBOUNDED
    } else if (optimizationStatus == GRB.OPTIMAL) {
      _solution = Array.tabulate(numberOfVars)(col => underlyingSolver.getVar(col).get(GRB.DoubleAttr.X))
      _objectiveValue = Some(underlyingSolver.get(GRB.DoubleAttr.ObjVal))
      SolutionStatus.OPTIMAL
    } else if (optimizationStatus == GRB.INFEASIBLE) {
      underlyingSolver.computeIIS()
      SolutionStatus.INFEASIBLE
    } else if (optimizationStatus == GRB.UNBOUNDED) {
      SolutionStatus.UNBOUNDED
    } else {
      _solution = Array.tabulate(numberOfVars)(col => underlyingSolver.getVar(col).get(GRB.DoubleAttr.X))
      logger.info("Optimization stopped with status = " + optimizationStatus)
      SolutionStatus.SUBOPTIMAL
    }

    _solutionStatus
  }

  /**
    * Release memory associated to the problem and the environment as well as
    * the gurobi license.
    */
  def release(): Unit = {
    underlyingSolver.dispose()
    underlyingSolver.getEnv.release()
    underlyingSolver.getEnv.dispose()
  }

  /**
    * Set a time limit for solver optimization. After the limit
    * is reached the solver stops running.
    *
    * @param limit the time limit
    */
  def setTimeout(limit: Int): Unit = {
    require(0 <= limit)
    underlyingSolver.getEnv.set(GRB.DoubleParam.TimeLimit, limit.toDouble)
  }
}
