import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
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

        assertFails { ClientSolverFactory.connectToSolver(solverID) }

        server.stop(true)
        server.awaitTermination()
        server.start()

        assertFails { ClientSolverFactory.connectToSolver(solverID) }
    }

    @Test
    @Throws(Exception::class)
    fun connectAndReadFromStdOutWithLaterJoin() {
        clients[BASIC]!!.solve("f(X), write(${OutputStore.STDOUT}, X)", SolveOptions.allEagerly())
        server.stop()
        server.awaitTermination()
        assertFails {
            ClientSolverFactory.connectToSolver(clients[BASIC]!!.getId())
            println("THIS SHOULD NOT BE PRINTED")
        }
        server.start()
        println("Waiting for service to start again...")
        Thread.sleep(3000)
        val solver = ClientSolverFactory.connectToSolver(clients[BASIC]!!.getId())!!
        val result = solver.readStreamOnOutputChannel(OutputStore.STDOUT)
        solver.solveOnce("close(${OutputStore.STDOUT})")
        assertContentEquals(
            listOf("b","d"),
            result)
    }
}
