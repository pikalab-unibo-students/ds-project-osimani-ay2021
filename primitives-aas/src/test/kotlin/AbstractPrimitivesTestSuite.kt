import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import examples.innestedPrimitive
import examples.ntPrimitive
import examples.readerPrimitive
import examples.throwablePrimitive
import io.grpc.Server
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.MutableSolver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.theory.Theory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

abstract class AbstractPrimitivesTestSuite {

    private val executor = Executors.newCachedThreadPool()
    private val primitivesHost = "localhost"
    private val activeServicesPorts = mutableMapOf<Int, Server>()

    abstract val primitives: List<DistributedPrimitiveWrapper>
    protected lateinit var solver: MutableSolver
    protected val log: MutableList<String> = mutableListOf()

    @BeforeTest
    fun beforeEach() {
        var port = 8080
        primitives.forEach {
            executor.submit {
                val service = PrimitiveServerFactory.startService(it, port, "customLibrary")
                activeServicesPorts[port] = service
                service.awaitTermination()
            }
            Thread.sleep(3000)
            port++
        }

        logicProgramming {
            solver = Solver.prolog.mutableSolverWithDefaultBuiltins(
                otherLibraries = Runtime.of(
                    Library.of("customLibrary",
                        activeServicesPorts.keys.associate {
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

    @AfterTest
    fun afterEach() {
        executor.shutdownNow()
        activeServicesPorts.values.forEach {
            it.shutdownNow()
        }
        activeServicesPorts.clear()
        log.clear()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }
}