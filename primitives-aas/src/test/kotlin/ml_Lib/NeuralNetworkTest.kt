package ml_Lib

import PythonPrimitivesTestSuite
import it.unibo.tuprolog.dsl.theory.logicProgramming
import kotlin.test.*

class NeuralNetworkTest: PythonPrimitivesTestSuite() {

    @Test
    @Throws(Exception::class)
    fun testInputLayer() {
        logicProgramming {
            val query = "input_layer"(1, X)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDenseLayer() {
        logicProgramming {
            val query = "input_layer"(1, X) and "dense_layer"(X,1, "relu", Y)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testOutputLayer() {
        logicProgramming {
            val query = "input_layer"(1, X) and
                "output_layer"(X,1, "sigmoid", Y)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testNeuralNetworkBuild() {
        logicProgramming {
            val query = "input_layer"(1, X) and
                "dense_layer"(X,1, "relu", Y) and
                "output_layer"(Y,1, "sigmoid", W) and
                "neural_network"(W, Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }
}