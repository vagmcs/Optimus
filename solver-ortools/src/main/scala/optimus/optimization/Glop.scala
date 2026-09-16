package optimus.optimization

/**
 * GLOP solver, accessed through the Google OR-Tools linear solver wrapper.
 *
 * @note GLOP is a pure linear programming solver, it has no notion of integrality.
 *       Integer and binary variable declarations are accepted but ignored, and the
 *       linear relaxation of the problem is solved instead.
 */
final class Glop() extends OrToolsSolver("GLOP")
