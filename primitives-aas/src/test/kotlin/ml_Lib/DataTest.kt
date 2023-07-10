package ml_Lib

import PythonPrimitivesTestSuite
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class DataTest() : PythonPrimitivesTestSuite() {

    val schemaName = "testSchema"
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
    fun testTheoryToSchema() {
        logicProgramming {
            val query = "theoryToSchema"(X)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            assertTrue(solution.substitution.contains(X))
            println(solution.substitution[X])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetSchema() {
        logicProgramming {
            val id = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val query = "schema"(id, X, Y, Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            assertEquals(Term.parse(schemaName), solution.substitution[X])
            assertEquals(Term.parse("[$target]"), solution.substitution[Z])
            println(solution.substitution[Y])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateSchema() {
        logicProgramming {
            val schemaName = "testSchema"
            val target = "hello"
            val query = "schema"(X, schemaName, listOf(
                "attribute"(0, "greet", "integer"),
                "attribute"(1, "hello", "categorical"(listOf("red", "yellow")))
            ), listOf(target))
            val solution1 = solver.solveOnce(query)
            assertTrue(solution1.isYes)
            assertTrue(solution1.substitution.contains(X))

            val id = solution1.substitution[X]!!.asTerm()
            val query2 = "schema"(id, X, Y, Z)
            val solution2 = solver.solveOnce(query2)
            assertTrue(solution2.isYes)
            assertEquals(Term.parse(schemaName), solution2.substitution[X])
            assertEquals(Term.parse("[$target]"), solution2.substitution[Z])
            println(solution2.substitution[Y])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTheoryToDataset() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testRandomSplit() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X) and "random_split"(X, 0.8, Y, Z)
            val solution = solver.solveOnce(query)
            assertTrue(solution.isYes)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFold() {
        logicProgramming {
            val k = 5
            val query = "theory_to_dataset"(schemaName, X) and "fold"(X, k, Y, Z)
            val solution = solver.solveList(query)
            assertTrue(solution.all { it.isYes })
            assertEquals(k, solution.size)
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSingleRow() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X) and "row"(X, 3, Z)
            val solution = solver.solveList(query)
            assertEquals(1, solution.size)
            assertTrue(solution.first().isYes )
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleRows() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X) and "row"(X, Y, Z)
            val solution = solver.solveList(query)
            assertEquals(solver.solveList(schemaName(X, Y, Z)).size, solution.size)
            assertTrue(solution.first().isYes )
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSingleColumn() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X) and "column"(X, 1, Z)
            val solution = solver.solveList(query)
            assertEquals(1, solution.size)
            assertTrue(solution.first().isYes )
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleColumns() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, X) and "column"(X, Y, Z)
            val solution = solver.solveList(query)
            assertEquals(solver.solveList("attribute"(X, Y, Z)).size, solution.size)
            assertTrue(solution.first().isYes )
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSingleCell() {
        logicProgramming {
            val query = "theory_to_dataset"(schemaName, K) and "cell"(K, 2, 2, Z)
            val solution = solver.solveList(query)
            assertEquals(1, solution.size)
            assertTrue(solution.first().isYes )
            println(solution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testTheoryFromDataset() {
        logicProgramming {
            val schemaId = solver.solveOnce("theoryToSchema"(X)).substitution[X]!!.asTerm()
            val datasetId = solver.solveOnce("theory_to_dataset"(schemaName, X)).substitution[X]!!.asTerm()
            solver.resetStaticKb()
            val query = "theory_from_dataset"(schemaId, datasetId)
            val solution = solver.solveOnce(query)
            println(solution)
            assertTrue(solution.isYes)
            assertTrue(solver.dynamicKb.isNonEmpty)
            solver.dynamicKb.forEach { println(it) }
        }
    }
}