import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.channel.OutputStore
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import kotlin.test.*


class ChannelOperationsTest {
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
        clients.values.forEach { it.closeClient() }
        server.stop(true)
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

    @Test
    @Throws(Exception::class)
    fun initializeWithStdout() {
        val result = mutableListOf<String>()
        clients[BASIC] = ClientSolverFactory.solverOf(staticKb = DEFAULT_STATIC_THEORY, libraries = setOf("prolog.io"),
            outputs = mapOf(Pair(OutputStore.STDOUT) { result.add(it) }))
        clients[BASIC]!!.solve("f(X), write(${OutputStore.STDOUT}, X)", SolveOptions.allEagerly())
        clients[BASIC]!!.solveOnce("close(${OutputStore.STDOUT})")
        println(result)
        assertContentEquals(
            listOf("b","d"),
            result)
    }

    @Test
    @Throws(Exception::class)
    fun readFromStdOutWithLaterJoin() {
        clients[BASIC]!!.solve("f(X), write(${OutputStore.STDOUT}, X)", SolveOptions.allEagerly())
        val result = clients[BASIC]!!.readStreamOnOutputChannel(OutputStore.STDOUT)
        clients[BASIC]!!.solveOnce("close(${OutputStore.STDOUT})")
        assertContentEquals(
            listOf("b","d"),
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
}
