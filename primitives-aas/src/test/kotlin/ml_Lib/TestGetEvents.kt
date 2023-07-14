package ml_Lib

import KotlinPrimitivesTestSuite
import examples.filterKBPrimitive
import examples.getEventsPrimitive
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class TestGetEvents: KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> =
        listOf(filterKBPrimitive, getEventsPrimitive)

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testEvents() {
        logicProgramming {
            val query = Struct.of("testEvents")
            assertTrue(solver.solveOnce(query).isYes)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFilterKB() {
        logicProgramming {
            solver.appendStaticKb(
                Theory.of(
                    Clause.of("p"("a")),
                    Clause.of("f"("b")),
                    Clause.of("f"("c"))
                )
            )
            val query = "filterKB"(Term.parse("f"), X)
            val solution = solver.solveList(query)
            assertTrue(solution.size >= 2)
            solution.forEach { println(it) }
        }
    }
}