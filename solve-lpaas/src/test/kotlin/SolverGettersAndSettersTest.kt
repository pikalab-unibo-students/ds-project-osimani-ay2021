import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import kotlin.test.*

import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY_STRING


class SolverGettersAndSettersTest {

    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.solverOf(staticKb = DEFAULT_STATIC_THEORY, libraries = setOf("IOLib"))
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
    fun testInAndOut() {
        clients[BASIC]!!.writeOnInputChannel("\$current", "message")
        val result = mutableListOf<String>()
        for (i in 0 until "message".length ) {
           clients[BASIC]!!.solveOnce("get_char(X), write(X)")
           result.add(clients[BASIC]!!.readOnOutputChannel("\$current"))
        }
        /** Solve closing stream, write on demand, etc **/
        clients[BASIC]!!.closeClient()
        assertContentEquals(
            listOf("m","e","s","s","a","g","e"),
            result)
    }
}