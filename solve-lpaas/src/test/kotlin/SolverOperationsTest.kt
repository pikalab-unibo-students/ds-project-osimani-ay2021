import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import kotlin.test.*

class SolverOperationsTest {

    private var clients: MutableMap<String, ClientSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"
    private val BLOCKING: String = "blocking"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = ClientSolver.prolog.solverOf(staticKb = DEFAULT_STATIC_THEORY)
    }

    @AfterTest
    fun afterEach() {
        server.stop()
        clients.values.forEach { it.closeClient() }
    }

    /** Testing Simple Solve **/
    @Test
    @Throws(Exception::class)
    fun simpleSolve() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        assertEquals(
            Struct.of("f", Term.parse("b")),
            sequence.next().solvedQuery
        )
        /** A solveIterator must always be completed, either completely consuming it or closing it **/
    }

    /** A failed test doesn't end if the stream is not closed before
     * Error with index = 0 **/
    @Test
    @Throws(Exception::class)
    fun solveIndex() {
        val sequence = clients[BASIC]!!.solve("f(X)")

        assertEquals(
            Struct.of("f", Term.parse("b")),
            sequence.getSolution(0).solvedQuery
        )
    }

    /** FIX **/
    @Test
    @Throws(Exception::class)
    fun solveIndexOutOfBounds() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        assert(sequence.getSolution(2).isNo)
    }

    /** Testing Factory of Solvers **/
    @Test
    @Throws(Exception::class)
    fun createSolver() {
        clients[BLOCKING] = ClientSolver.prolog.solverOf(staticKb = Theory.parse("""
                   p(a, b).
                   """.trimIndent()))
        val sequence = clients[BLOCKING]!!.solve("p(X, Y)")
        assertEquals(
            Struct.of("p", Term.parse("a"), Term.parse("b")),
            sequence.next().solvedQuery
        )

    }

    /** Testing Stream-Like Nature of Solve **/
    @Test
    @Throws(Exception::class)
    fun testStreamLikeResponse() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        val solutions = mutableListOf<Solution>()
        while (sequence.hasNext()) {
            solutions.add(sequence.next())
        }
        assertEquals(
            listOf(Struct.of("f", Term.parse("b")),
                Struct.of("f", Term.parse("d"))),
            solutions.map { it.solvedQuery }
        )

        assert(!sequence.hasNext())
    }

    /** Testing async-nature of requests FIX Lazy is not default **/
    @Test
    @Throws(Exception::class)
    fun multipleConcurrentRequests() {
        clients[BLOCKING] = (ClientSolver.prolog.solverOf(staticKb = Theory.parse("""
                      p(X):-p(X).
                      """.trimIndent())))
        clients[BLOCKING]!!.solve("p(X)")
        val sequence = clients[BASIC]!!.solve("f(X)")
        assertEquals(
            Struct.of("f", Term.parse("b")),
            sequence.next().solvedQuery
        )
    }

    /** Testing SolveAsList FIX**/
    @Test
    @Throws(Exception::class)
    fun solveQueryList() {
        val result = clients[BASIC]!!.solveList("f(X)")
        assertContentEquals(
            listOf(Struct.of("f", Term.parse("b")), Struct.of("f", Term.parse("d"))),
            result.map {it.solvedQuery}
        )
    }

    /** Testing Solve Once **/
    @Test
    @Throws(Exception::class)
    fun solveOnceQuery() {
        val result = clients[BASIC]!!.solveOnce("f(X)")
        assertEquals(
            Struct.of("f", Term.parse("b")),
            result.solvedQuery
        )
    }

    /** Testing Solve With Timeout */
    @Test
    @Throws(Exception::class)
    fun solveQueryWithTimeout() {
        clients[BLOCKING] = (ClientSolver.prolog.solverOf(staticKb = Theory.parse("""
                      p(a):- sleep(3000).
                      """.trimIndent())))
        val result = clients[BLOCKING]!!.solve("p(X)", 1000).getSolution(0)
        assertContains(
            result.exception.toString(), "TimeOutException"
        )
        assert(result.isHalt)
    }

    /** Testing Solve With Timeout */
    @Test
    @Throws(Exception::class)
    fun solveQueryWithLimit() {
        clients[BLOCKING] = (ClientSolver.prolog.solverOf(staticKb = Theory.parse("""
                      p(a).
                      p(b).
                      p(c).
                      """.trimIndent())))
        val result = clients[BLOCKING]!!.solve("p(X)", SolveOptions.someLazily(2)).asSequence().toList()
        assertContentEquals(
            listOf(
                Struct.of("p", Term.parse("a")),
                Struct.of("p", Term.parse("b"))),
            result.map {it.solvedQuery}
        )
    }

    /** Testing Solve List With Timeout FIX**/
    @Test
    @Throws(Exception::class)
    fun solveQueryAsListWithTimeout() {
        clients[BLOCKING] = (ClientSolver.prolog.solverOf(staticKb = Theory.parse("""
                      p(a).
                      p(b).
                      p(c):- sleep(3000).
                      """.trimIndent())))
        val result = clients[BLOCKING]!!.solveList("p(X)", 1000)
        assertContentEquals(
            listOf(
                Struct.of("p", Term.parse("a")),
                Struct.of("p", Term.parse("b")),
                null),
            result.map {it.solvedQuery}
        )
    }
}