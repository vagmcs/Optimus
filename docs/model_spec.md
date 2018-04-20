TODO

```scala
class MySpec extends ModelSpec(SolverLib.oJSolver) {

    val x = MPFloatVar("x", 100, 200)
    val y = MPFloatVar("y", 80, 170)
    
    maximize(-2 * x + 5 * y)
    add(y >:= -x + 200)
    
    start()
    
    release()
}
```