import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import io.grpc.Server
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

abstract class AbstractPrimitivesTestSuite {

    protected val executor: ExecutorService = Executors.newCachedThreadPool()
    private val primitivesHost = "localhost"

    protected lateinit var solver: MutableSolver
    protected val log: MutableList<String> = mutableListOf()

    @BeforeTest
    open fun beforeEach() {
        logicProgramming {
            solver = Solver.prolog.mutableSolverWithDefaultBuiltins(
                otherLibraries = Runtime.of(
                    Library.of("customLibrary",
                        getActivePorts().associate {
                            PrimitiveClientFactory.connectToPrimitive(primitivesHost, it)
                        }
                    )
                ),
                stdIn = InputChannel.of("hello"),
                stdOut = OutputChannel.of {
                    log.add(it)
                }
            )
        }
    }

    abstract fun getActivePorts(): Set<Int>

    @AfterTest
    open fun afterEach() {
        executor.shutdownNow()
        log.clear()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }
}