package variablesAssignment

import AbstractPrimitivesTestSuite
import examples.customSumPrimitive
import examples.getEventsPrimitive
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import examples.innestedPrimitive
import io.grpc.Server
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

class TestVariablesAssignment: AbstractPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> = listOf(innestedPrimitive, customSumPrimitive)

    @Test
    @Throws(Exception::class)
    fun testCustomSum() {
        logicProgramming {
            val solution = solver.solveOnce("customSum"(1, 2, X))
            assertTrue(solution.isYes)
            assertEquals(Substitution.of(X, Numeric.of(3.0)), solution.substitution)
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testSubSolve() {
        logicProgramming {
            val solution = solver.solveOnce("solve"("natural"(X)))
            assertTrue(solution.isYes)
            assertEquals(Substitution.of(X, Numeric.of(0)), solution.substitution)
        }
    }

}