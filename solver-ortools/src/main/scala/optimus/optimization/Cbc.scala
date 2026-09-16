package optimus.optimization

/** COIN-OR CBC solver, accessed through the Google OR-Tools linear solver wrapper. */
final class Cbc() extends OrToolsSolver("CBC")
