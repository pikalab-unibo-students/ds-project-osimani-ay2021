import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import kotlin.test.*

import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.libs.io.IOLib
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


class SolverGettersAndSettersTest {

    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.basicClient()
    }

    @AfterTest
    fun afterEach() {
        server.stop()
        clients.values.forEach { it.closeClient() }
    }

    @Test
    @Throws(Exception::class)
    fun getStaticKB() {
        val theory = clients[BASIC]!!.getStaticKB()
        assertEquals(
            DEFAULT_STATIC_THEORY,
            theory
        )
    }

    @Test
    @Throws(Exception::class)
    fun getOperatorSet() {
        val messages = mutableListOf<String>()
        val solver = Solver.prolog.mutableSolverWithDefaultBuiltins (
            otherLibraries = Runtime.of(IOLib),
            stdIn = InputChannel.of("hello"),
            stdOut = OutputChannel.of { messages += it }
        )

        //solver.inputChannels.setStdIn(InputChannel.of("hello"))
        solver.setStandardInput(InputChannel.of("byeee"))// = solver.inputChannels.plus(Pair("second", )))
        runBlocking { delay(1000) }
        println(solver.inputChannels)
        val goal = Scope.empty {
            tupleOf (
                structOf ("get_char", varOf ("X")),
                structOf ("write", varOf ("X"))
            )
        } // ?- get_char (X), write (X).
        for (i in 0 until "hello".length*2 ) {
            println(solver.solveOnce(goal)) // X=h, X=e, X=l, ...
        }
        println ( messages ) // [h, e, l, l, o]
    }
}