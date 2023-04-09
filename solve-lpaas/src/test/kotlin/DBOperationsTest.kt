import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import kotlin.test.*


class DBOperationsTest {
    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service
    private val BASIC: String = "basic"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.solverOf(staticKb = DEFAULT_STATIC_THEORY, libraries = setOf("prolog.io"))
    }

    @AfterTest
    fun afterEach() {
        clients.values.forEach { it.closeClient() }
        server.stop(true)
    }

    @Test
    @Throws(Exception::class)
    fun deleteClient() {
        val solverID = clients[BASIC]!!.getId()
        clients[BASIC]!!.closeClient(true)
        assertFails { clients[BASIC]!!.getStaticKB() }

        assertFails { ClientPrologSolverFactory.connectToSolver(solverID) }

        server.stop(true)
        server.awaitTermination()
        server.start()

        assertFails { ClientPrologSolverFactory.connectToSolver(solverID) }
    }

    @Test
    @Throws(Exception::class)
    fun failureOfMutableMethods() {
        val solver = ClientPrologSolverFactory.mutableSolverOf()
        solver.closeClient(true)
        assertFails { solver.loadLibrary("x") }
        assertFails { solver.unloadLibrary("x") }
        assertFails { solver.setRuntime(setOf()) }
        assertFails { solver.loadStaticKb(Theory.empty()) }
        assertFails { solver.appendStaticKb(Theory.empty()) }
        assertFails { solver.resetStaticKb() }
        assertFails { solver.loadDynamicKb(Theory.empty()) }
        assertFails { solver.appendDynamicKb(Theory.empty()) }
        assertFails { solver.resetDynamicKb() }
        val struct = Struct.of("f", Term.parse("a"))
        assertFails { solver.assertA(struct) }
        assertFails { solver.assertZ(struct) }
        assertFails { solver.retract(struct) }
        assertFails { solver.retractAll(struct) }
        assertFails { solver.setFlag("f", struct) }
        assertFails { solver.setStandardInput("hello") }
        //These should print the error
        solver.setStandardOutput(OutputChannel.stdOut())
        solver.setStandardError(OutputChannel.stdOut())
        solver.setWarnings(OutputChannel.stdOut())
    }

    @Test
    @Throws(Exception::class)
    fun failureOfMethods() {
        clients[BASIC]!!.closeClient(true)
        assertFails { clients[BASIC]!!.solve("x") }
        assertFails { clients[BASIC]!!.getFlags() }
        assertFails { clients[BASIC]!!.getStaticKB() }
        assertFails { clients[BASIC]!!.getDynamicKB() }
        assertFails { clients[BASIC]!!.getLibraries() }
        assertFails { clients[BASIC]!!.getUnificator() }
        assertFails { clients[BASIC]!!.getOperators() }
        assertFails { clients[BASIC]!!.getInputChannels() }
        assertFails { clients[BASIC]!!.getOutputChannels() }
        assertFails { clients[BASIC]!!.readOnOutputChannel("hello") }
        //These should print the error
        clients[BASIC]!!.writeOnInputChannel("hello")
        clients[BASIC]!!.readStreamOnOutputChannel("hello")
    }

    @Test
    @Throws(Exception::class)
    fun connectAndReadFromStdOutWithLaterJoin() {
        clients[BASIC]!!.solve("f(X), write(${OutputStore.STDOUT}, X)", SolveOptions.allEagerly())
        server.stop()
        server.awaitTermination()
        assertFails {
            ClientPrologSolverFactory.connectToSolver(clients[BASIC]!!.getId())
            println("THIS SHOULD NOT BE PRINTED")
        }
        server.start()
        println("Waiting for service to start again...")
        Thread.sleep(3000)
        val solver = ClientPrologSolverFactory.connectToSolver(clients[BASIC]!!.getId())!!
        val result = solver.readStreamOnOutputChannel(OutputStore.STDOUT)
        solver.solveOnce("close(${OutputStore.STDOUT})")
        assertContentEquals(
            listOf("b","d"),
            result)
    }
}
