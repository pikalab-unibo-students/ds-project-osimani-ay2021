package sideEffects

import KotlinPrimitivesTestSuite
import examples.customAssertPrimitive
import examples.customWritePrimitive
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.channel.OutputStore
import kotlin.test.*

class TestSideEffects: KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> =
        listOf(customAssertPrimitive, customWritePrimitive)

    @Test
    @Throws(Exception::class)
    fun testAssert() {
        logicProgramming {
           assertTrue(solver.solveOnce("p"(X)).isNo)
            val query = "customAssert"(Clause.of("p"("a")))
            solver.solveOnce(query)
            val solution = solver.solveOnce("p"(X))
            assertEquals(
                Substitution.of(X, Term.parse("a")),
                solution.substitution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testWrite() {
        logicProgramming {
            val message = "hello"
            val query = "customWrite"(OutputStore.STDOUT, message)
            solver.solveOnce(query)
            assertEquals(message, log[0])
        }
    }
}