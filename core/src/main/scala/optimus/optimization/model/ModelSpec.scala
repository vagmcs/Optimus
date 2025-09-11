package optimus.optimization.model

import optimus.optimization.MPModel
import optimus.optimization.enums.SolverLib

abstract class ModelSpec(solverLib: SolverLib) {
  implicit protected val model: MPModel = MPModel(solverLib)
}
