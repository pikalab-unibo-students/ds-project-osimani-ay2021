package backtracking

import AbstractPrimitivesTestSuite
import examples.*
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

class TestBacktracking: AbstractPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> =
        listOf(innestedPrimitive, ntPrimitive, readerPrimitive, throwablePrimitive, writerPrimitive)

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testNt() {
        logicProgramming {
            val query = "nt"(X)
            val solutions = solver.solve(query).take(4).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "nt"(0),
                    "nt"(1),
                    "nt"(2),
                    "nt"(3),
                ).toList(),
                solutions
            )
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testSubSolve() {
        logicProgramming {
            val query = "solve"("natural"(X))
            val solutions = solver.solve(query).take(2).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "solve"("natural"(0)),
                    "solve"("natural"(1)),
                ).toList(),
                solutions
            )
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testReadLine() {
        val solutions = logicProgramming {
            val query = "readLine"(InputStore.STDIN, X)
            solver.solve(query).take(6).toList()
        }
        assertTrue { solutions.last().isNo }
        assertEquals(
            listOf(
                "h", "e", "l", "l", "o"
            ).toList(),
            solutions.take(5).map {
                it.substitution.values.first().toString()
            }
        )
    }
}