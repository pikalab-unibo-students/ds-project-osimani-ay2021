package variablesAssignment

import KotlinPrimitivesTestSuite
import examples.customSumPrimitive
import examples.innestedPrimitive
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*

class TestVariablesAssignment: KotlinPrimitivesTestSuite() {

    override val primitives: List<DistributedPrimitiveWrapper> = listOf(innestedPrimitive, customSumPrimitive)

    @Test
    @Throws(Exception::class)
    fun testCustomSum() {
        logicProgramming {
            val solution = solver.solveOnce("customSum"(1, 2, X))
            assertTrue(solution.isYes)
            assertEquals(Substitution.of(X, Numeric.of(3)), solution.substitution)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCustomSum2() {
        logicProgramming {
            solver.appendStaticKb(Theory.Companion.of(
                rule{
                    "summing"(X) `if` "customSum"(1, 2, X)
                }
            ))
            val solution = solver.solveOnce("summing"(X))
            assertTrue(solution.isYes)
            assertEquals(Substitution.of(X, Numeric.of(3)), solution.substitution)
            print(solution)
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