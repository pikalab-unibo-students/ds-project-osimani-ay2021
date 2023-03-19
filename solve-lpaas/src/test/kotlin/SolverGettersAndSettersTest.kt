import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import kotlin.test.*

import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY_STRING
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse


class SolverGettersAndSettersTest {

    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"
    private val MUTABLE: String = "mutable"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.solverOf(staticKb = DEFAULT_STATIC_THEORY, libraries = setOf("IOLib"))
        clients[MUTABLE] = ClientSolver.prolog
            .mutableSolverOf(dynamicKb = DEFAULT_STATIC_THEORY, libraries = setOf("IOLib"),
                inputChannels = mapOf(Pair("stdin","miiii")), defaultBuiltins = true)
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
    fun useRetract() {
        val result = (clients[MUTABLE]!! as ClientMutableSolver)
            .retract(Struct.of("f", Term.parse("X")))
        assertEquals(Theory.parse("f(d) :- true."),
            result.theory)
        assertContentEquals(
            listOf(Clause.parse("f(b) :- true.")),
            result.clauses)
    }

    @Test
    @Throws(Exception::class)
    fun useRetractAll() {
        val result = (clients[MUTABLE]!! as ClientMutableSolver)
            .retractAll(Struct.of("f", Term.parse("X")))
        assertEquals(Theory.empty(),
            result.theory)
        assertContentEquals(
            listOf(Clause.parse("f(b) :- true."), Clause.parse("f(d) :- true.")),
            result.clauses)
    }

    @Test
    @Throws(Exception::class)
    fun useSetStdIn() {
        (clients[MUTABLE]!! as ClientMutableSolver)
            .setStandardInput("bye", "hello")
        val result = mutableListOf<String>()
        for (i in 0 until "hello".length ) {
            clients[MUTABLE]!!.solveOnce("get_char(stdin, X), write(stdout, X)")
            result.add(clients[MUTABLE]!!.readOnOutputChannel("stdout"))
        }
        /** Solve closing stream, write on demand, etc **/
        clients[BASIC]!!.closeClient()
        assertContentEquals(
            listOf("h","e","l","l","o"),
            result)
    }

    @Test
    @Throws(Exception::class)
    fun testInAndOutChannel() {
        clients[BASIC]!!.writeOnInputChannel("stdin").onNext("message")
        val result = mutableListOf<String>()
        for (i in 0 until "message".length ) {
            clients[BASIC]!!.solveOnce("get_char(stdin, X), write(stdout, X)")
            result.add(clients[BASIC]!!.readOnOutputChannel("stdout"))
        }
        /** Solve closing stream, write on demand, etc **/
        clients[BASIC]!!.closeClient()
        assertContentEquals(
            listOf("m","e","s","s","a","g","e"),
            result)
    }

    @Test
    @Throws(Exception::class)
    fun testInAndOutStreamChannel() {
        clients[BASIC]!!.writeOnInputChannel("stdin").onNext("message")
        val result = clients[BASIC]!!.readStreamOnOutputChannel("stdout")
        for (i in 0 until "message".length ) {
            clients[BASIC]!!.solveOnce("get_char(stdin, X), write(stdout, X)")
        }
        /** Solve closing stream, write on demand, etc **/
        clients[BASIC]!!.closeClient()
        assertContentEquals(
            listOf("m","e","s","s","a","g","e"),
            result)
    }
}