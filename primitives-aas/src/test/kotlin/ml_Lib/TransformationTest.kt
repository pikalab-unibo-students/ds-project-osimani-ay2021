package ml_Lib

import PythonPrimitivesTestSuite
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class TransformationTest: PythonPrimitivesTestSuite() {

    private val schemaName = "testSchema"
    private val target = "bye"

    @BeforeTest
    override fun beforeEach() {
        super.beforeEach()
        logicProgramming {
            solver.appendStaticKb(
                Theory.of(
                    fact { "attribute"(0, "greet", "integer") },
                    fact { "attribute"(1, "hello", "categorical"(listOf("red", "yellow"))) },
                    fact { "attribute"(2, target, "integer") },
                    fact { "schema_name"(schemaName) },
                    fact { "schema_target"(arrayOf(target)) },
                    fact { schemaName(1, "yellow", 0) },
                    fact { schemaName(2, "red", 1) },
                    fact { schemaName(3, "red", 0) },
                    fact { schemaName(4, "yellow", 1) },
                    fact { schemaName(5, "red", 0) },
                    fact { schemaName(6, "yellow", 1) },
                    fact { schemaName(7, "yellow", 0) },
                    fact { schemaName(8, "red", 1) },
                    fact { schemaName(9, "yellow", 0) },
                    fact { schemaName(10, "red", 1) }
                )
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSchemaTransformation() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema_trasformation"(id, X)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testNormalize() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema_trasformation"(id, X) and "normalize"(X, arrayOf("greet"), Y)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Y]!!.asTerm()
            val query2 = "schema_trasformation"(X, transfId) and "schema"(X, Y, W, Z)
            val solution2 = solver.solveOnce(query2)
            assertTrue(solution2.isYes)
            println(solution2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testOneHotEncode() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema_trasformation"(id, X) and "one_hot_encode"(X, arrayOf("hello"), Y)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Y]!!.asTerm()
            val query2 = "schema_trasformation"(X, transfId) and "schema"(X, Y, W, Z)
            val solution2 = solver.solveOnce(query2)
            assertTrue(solution2.isYes)
            println(solution2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDropEncode() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema_trasformation"(id, X) and "drop"(X, arrayOf("hello"), Y)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Y]!!.asTerm()
            val query2 = "schema_trasformation"(X, transfId) and "schema"(X, Y, W, Z)
            val solution2 = solver.solveOnce(query2)
            assertTrue(solution2.isYes)
            println(solution2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFit() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema_trasformation"(id, X) and
                "normalize"(X, "greet", Y) and
                "one_hot_encode"(Y, "hello", Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Z]!!.asTerm()
            val query2 = "theory_to_dataset"(schemaName, X) and
                "fit"(transfId, X, Y) and
                "schema_trasformation"(Z, Y) and
                "schema"(Z, A, B, C)
            val solution2 = solver.solveOnce(query2)
            assertTrue(solution2.isYes)
            println(solution2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTransformApply() {
        logicProgramming {
            val query = "theoryToSchema"(S) and
                "theory_to_dataset"(schemaName, D) and
                "schema_trasformation"(S, X) and
                "normalize"(X, "greet", Y) and
                "one_hot_encode"(Y, "hello", W) and
                "fit"(W, D, Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Z]!!.asTerm()
            val datasetId = solution.substitution[D]!!.asTerm()
            val query2 = "transform"(datasetId, transfId, X)
            val solution2 = solver.solveOnce(query2)
            println(solution2)
            assertTrue(solution2.isYes)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTransformInvert() {
        logicProgramming {
            val query = "theoryToSchema"(S) and
                "theory_to_dataset"(schemaName, D) and
                "schema_trasformation"(S, X) and
                "normalize"(X, "greet", Y) and
                "one_hot_encode"(Y, "hello", W) and
                "fit"(W, D, Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            val transfId = solution.substitution[Z]!!.asTerm()
            val datasetId = solution.substitution[D]!!.asTerm()
            val query2 = "transform"(datasetId, transfId, X) and "transform"(Y, transfId, X)
            val solution2 = solver.solveOnce(query2)
            println(solution2)
            assertTrue(solution2.isYes)
        }
    }
}