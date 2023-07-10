package ml_Lib

import AbstractPrimitivesTestSuite
import PythonPrimitivesTestSuite
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class PredictorTest: PythonPrimitivesTestSuite() {

    private val schemaName = "testSchema"
    private val target = arrayOf("odd", "even")
    @BeforeTest
    override fun beforeEach() {
        super.beforeEach()
        logicProgramming {
            solver.appendStaticKb(
                Theory.of(
                    //fact { "attribute"(0, "greet", "integer") },
                    fact { "attribute"(1, "greet2", "real") },
                    fact { "attribute"(2, "odd", "real") },
                    fact { "attribute"(2, "even", "integer") },
                    fact { "schema_name"(schemaName) },
                    fact { "schema_target"(target) },
                    fact { schemaName(0, 0, 1) },
                    fact { schemaName(1, 1, 0) },
                    fact { schemaName(2, 0, 1) },
                    fact { schemaName(3, 1, 0) },
                    fact { schemaName(4, 0, 1) },
                    fact { schemaName(5, 1, 0) },
                    fact { schemaName(6, 0, 1) },
                    fact { schemaName(7, 1, 0) },
                    fact { schemaName(8, 0, 1) },
                    fact { schemaName(9, 1, 0) },
                    rule { "createModel"(D) `if` (
                        "input_layer"(1, A) and
                            "dense_layer"(A, 10, "relu", B) and
                            "output_layer"(B,2, "sigmoid", C) and
                            "neural_network"(C, D)
                        )},
                    rule { "getDataset"(X) `if` (
                        "theory_to_dataset"(schemaName, X)
                    )}
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFit() {
        logicProgramming {
            val query = "createModel"(X) and
                "getDataset"(Y) and
                "train"(X, Y, arrayOf(
                    "max_epoch"(5), "batch_size"(10),
                    "learning_rate"(0.01), "loss"("cross_entropy")
                ), W)
            val solution = solver.solveOnce(query)
            println(solution)
            assertTrue(solution.isYes)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testPredict() {
        logicProgramming {
            val query = "createModel"(X) and
                "getDataset"(Y) and
                "train"(X, Y, arrayOf(
                    "max_epoch"(5), "batch_size"(1),
                    "learning_rate"(0.001), "loss"("cross_entropy")
                ), W) and
                "predict"(W, Y, Z)
            val solution = solver.solveOnce(query)
            println(solution.substitution[Z])
            assertTrue(solution.isYes)
        }
    }

    @Ignore
    @Test
    @Throws(Exception::class)
    fun testClassify() {
        logicProgramming {
            val x = 3
            val query = "createModel"(X) and
                "getDataset"(Y) and
                "train"(X, Y, arrayOf(
                    "max_epoch"(10), "batch_size"(10),
                    "learning_rate"(0.00001), "loss"("cross_entropy")
                ), W) and
                "predict"(W, "row"(x), Z) and
                "classify"(Z, "argmax", arrayOf("odd", "even"), A)
            val solution = solver.solveOnce(query)
            println(solution.substitution[Z])
            assertEquals(if(x%2 == 0) "even" else "odd", solution.substitution[A].toString())
            assertTrue(solution.isYes)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testScore() {
        logicProgramming {
            val query = "createModel"(X) and
                "getDataset"(Y) and
                "train"(X, Y, arrayOf(
                    "max_epoch"(5), "batch_size"(10),
                    "learning_rate"(0.00001), "loss"("cross_entropy")
                ), W) and
                "predict"(W, Y, Z) and
                "mse"(Z,  arrayOf(
                    arrayOf(0, 1),
                    arrayOf(1, 0),
                    arrayOf(0, 1),
                    arrayOf(1, 0),
                    arrayOf(0, 1),
                    arrayOf(1, 0),
                    arrayOf(0, 1),
                    arrayOf(1, 0),
                    arrayOf(0, 1),
                    arrayOf(1, 0),
                ), A)
            val solution = solver.solveOnce(query)
            println(solution.substitution[A])
            assertTrue(solution.isYes)
        }
    }
}