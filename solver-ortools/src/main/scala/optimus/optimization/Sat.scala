package optimus.optimization

/**
 * CP-SAT solver, accessed through the Google OR-Tools linear solver wrapper.
 *
 * @note CP-SAT requires all variables to have finite bounds, unbounded problems
 *       are not supported.
 */
final class Sat() extends OrToolsSolver("SAT")
