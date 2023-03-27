import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.PrologSolverFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import kotlin.test.*


class SolverGettersAndSettersTest {

    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"
    private val MUTABLE: String = "mutable"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.solverOf(staticKb = DEFAULT_STATIC_THEORY, libraries = setOf("prolog.io"))
        clients[MUTABLE] = ClientSolver.prolog
            .mutableSolverOf(dynamicKb = DEFAULT_STATIC_THEORY, libraries = setOf("prolog.io"), defaultBuiltins = true)
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
    fun writeOnStdIn() {
        clients[BASIC]!!.writeOnInputChannel("stdin", "h", "e", "l", "l", "o")
        val result = mutableListOf<String>()
        for(i in 0 until "hello".length) {
            result.add(clients[BASIC]!!.solveOnce(
                "get_char(${InputStore.STDIN}, X)").solvedQuery.toString())
        }
        clients[BASIC]!!.solveOnce("close(${InputStore.STDIN})")
        assertContentEquals(
            "hello".toCharArray().map { "get_char(${InputStore.STDIN}, $it)" },
            result)
    }

    @Test
    @Throws(Exception::class)
    fun readFromStdOut() {
        val result = clients[BASIC]!!.readStreamOnOutputChannel(OutputStore.STDOUT)
        clients[BASIC]!!.solve("f(X), write(${OutputStore.STDOUT}, X)", SolveOptions.allEagerly())
        clients[BASIC]!!.solveOnce("close(${OutputStore.STDOUT})")
        assertContentEquals(
            listOf("b","d"),
            result)
    }

    /**This Test fails because the stdin is not changed in actuality**/
    @Test
    @Throws(Exception::class)
    fun useSetStdIn() {
        (clients[MUTABLE]!! as ClientMutableSolver)
            .setStandardInput("hello")
        val result = mutableListOf<String>()
        for (i in 0 until "hello".length ) {
            clients[MUTABLE]!!.solveOnce("get_char(stdin, X), write(stdout, X)")
            result.add(clients[MUTABLE]!!.readOnOutputChannel("stdout"))
        }
        /** Solve closing stream, write on demand, etc **/
        assertContentEquals(
            listOf("h","e","l","l","o"),
            result)
    }

    @Test
    @Throws(Exception::class)
    fun testInAndOutStreamChannel() {
        clients[BASIC]!!.writeOnInputChannel("stdin", "m", "e", "s", "s", "a", "g", "e")
        val result = clients[BASIC]!!.readStreamOnOutputChannel("stdout")
        for (i in 0 until "message".length ) {
            clients[BASIC]!!.solveOnce("get_char(stdin, X), write(stdout, X)")
        }
        /** Solve closing stream, write on demand, etc **/
        println(result)
        clients[BASIC]!!.closeClient()
        assert(listOf("m","e","s","s","a","g","e").containsAll(result))
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleConnectionsToSameSolver() {
        clients[BASIC]!!.solveOnce("assert(p(c))")
        clients["temp"] = PrologSolverFactory.connectToSolver(clients[BASIC]!!.getId())!!
        assertEquals(
            "p(c)",
            clients["temp"]!!.solveOnce("p(X)").solvedQuery.toString()
        )
    }
}