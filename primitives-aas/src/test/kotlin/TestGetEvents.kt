import examples.*
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.unify.Unificator
import java.util.concurrent.Executors
import kotlin.test.*

class TestGetEvents {

    private lateinit var solver: Solver
    private val executor = Executors.newCachedThreadPool()

    @BeforeTest
    fun beforeEach() {
        executor.submit {
            PrimitiveServerFactory
                .startService(getEventsPrimitive, 8081, "customLibrary")
        }
        Thread.sleep(3000)

        logicProgramming {
            solver = Solver.prolog.solverWithDefaultBuiltins(
                otherLibraries = Runtime.of(
                    PrimitiveClientFactory.searchLibrary("customLibrary")
                )
            )
        }
    }

    @AfterTest
    fun afterEach() {
        executor.shutdownNow()
    }


    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testEvents() {
        logicProgramming {
            val query = Struct.of("testEvents")
            val solution = solver.solve(query).first()
            println(solution)
            assertTrue(solution.isYes)
        }
    }
}