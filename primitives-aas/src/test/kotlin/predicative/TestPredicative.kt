package predicative

import KotlinPrimitivesTestSuite
import examples.*
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.exception.HaltException
import it.unibo.tuprolog.solve.exception.error.TypeError
import kotlin.test.*

class TestPredicative: KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> = listOf(
        innestedPrimitive, ntPrimitive, readerPrimitive,
        throwablePrimitive, writerPrimitive, customSumPrimitive
    )

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testNt() {
        logicProgramming {
            assertTrue(solver.solveOnce("nt"(0)).isYes)
            assertTrue(solver.solveOnce("nt"("a")).isNo)
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testSubSolve() {
        logicProgramming {
            assertTrue(solver.solveOnce("solve"("natural"(0))).isYes)
            assertTrue(solver.solveOnce("solve"("a")).isNo)
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testError() {
        logicProgramming {
            val solution = solver.solveOnce(Struct.of("error"))
            assertTrue(solution.isHalt)
            assertTrue(solution.exception is HaltException &&
                (solution.exception as HaltException).exitStatus == 404)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCustomSum() {
        logicProgramming {
            assertTrue(solver.solveOnce("customSum"(1, 2, 3)).isYes)
            assertTrue(solver.solveOnce("customSum"(1, 2, 4)).isNo)

            val faultingTerm = Struct.of("a")
            val solution = solver.solveOnce("customSum"(1, faultingTerm, 3))
            assertTrue(solution.isHalt)
            assertTrue(solution.exception is TypeError)
            val exception = solution.exception as TypeError
            assertEquals(exception.culprit, faultingTerm)
            assertEquals(exception.expectedType, TypeError.Expected.NUMBER)
        }
    }
}