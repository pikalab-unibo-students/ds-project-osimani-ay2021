import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.server.Service
import kotlin.test.*

class SolverTest {

    private var clients: MutableMap<String, SimpleSolver> = mutableMapOf()
    private lateinit var server: Service

    private val BASIC: String = "basic"
    private val BLOCKING: String = "blocking"

    @BeforeTest
    fun beforeEach() {
        server = Service()
        server.start()
        clients[BASIC] = SimpleSolver.prolog.basicClient()
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
        val solution = sequence.next()
        sequence.closeSequence()

        assertEquals(
            Struct.of("f", Term.parse("b")),
            solution.solvedQuery
        )
        /** A solveIterator must always be completed, either completely consuming it or closing it **/

    }


    /** A failed test doesn't end if the stream is not closed before
     * Error with index = 0 **/
    @Test
    @Throws(Exception::class)
    fun solveIndex() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        val solution = sequence.getSolution(0)
        sequence.closeSequence()

        assertEquals(
            Struct.of("f", Term.parse("b")),
            solution.solvedQuery
        )
    }

    @Test
    @Throws(Exception::class)
    fun solveIndexOutOfBounds() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        assertFailsWith(IndexOutOfBoundsException::class) {
            sequence.getSolution(3)
        }
        sequence.closeSequence()
    }

    /** Testing Factory of Solvers **/
    @Test
    @Throws(Exception::class)
    fun createSolver() {
        clients[BLOCKING] = SimpleSolver.prolog.basicClient("""
                   p(a, b).
                   """.trimIndent())
        val sequence = clients[BLOCKING]!!.solve("p(X, Y)")
        val solution = sequence.next()
        sequence.closeSequence()

        assertEquals(
            Struct.of("p", Term.parse("a"), Term.parse("b")),
            solution.solvedQuery
        )

    }

    /** Testing Stream-Like Nature of Solve **/
    @Test
    @Throws(Exception::class)
    fun testStreamLikeResponse() {
        val sequence = clients[BASIC]!!.solve("f(X)")
        val solutions = 0.rangeTo(2).map { sequence.next() }
        sequence.closeSequence()
        assertEquals(
            listOf(Struct.of("f", Term.parse("b")),
                Struct.of("f", Term.parse("d")), null),
            solutions.map { it.solvedQuery }
        )

        assert(solutions.last().isNo && !sequence.hasNext())
    }

    @Test
    @Ignore
    @Throws(Exception::class)
    fun failingRequest() {
        clients[BLOCKING] = SimpleSolver.prolog.basicClient("""
                   p(X):-p(X).
                   """.trimIndent())
        val sequence = clients[BLOCKING]!!.solve("p(X)")
        val solution = sequence.next()
        sequence.closeSequence()
        assert( solution.isNo )
    }

    /** Testing async-nature of requests FIX Lazy is not default **/
    @Test
    @Throws(Exception::class)
    fun multipleRequests() {
        clients[BLOCKING] = (SimpleSolver.prolog.basicClient("""
                      p(X):-p(X).
                      """.trimIndent()))
        clients[BLOCKING]!!.solve("p(X)").closeSequence()
        val sequence = clients[BASIC]!!.solve("f(X)")
        val solution = sequence.next().solvedQuery
        sequence.closeSequence()
        assertEquals(
            Struct.of("f", Term.parse("b")),
            solution
        )
    }

/*      /** Testing SolveAsList FIX**/
        @Test
        @Throws(Exception::class)
        fun solveQueryList() {
            val client = SimpleSolver.prolog.basicClient("""
                       p(a) :- sleep(3000).
                       p(c).
                       """.trimIndent())
            val result = client.solveList("p(X)")
            runBlocking {
                delay(1000)
            }
            assert(result.isEmpty())
            assertContentEquals(
                listOf(Struct.of("p", Term.parse("a")), Struct.of("p", Term.parse("c"))),
                result.map {it.solvedQuery}
            )
        }

        /** Testing Solve Once **/
        @Test
        @Throws(Exception::class)
        fun solveOnceQuery() {
            val result = basicSolver.solveOnce("f(X)").iterator()
            assertEquals(
                Struct.of("f", Term.parse("b")),
                result.next().solvedQuery
            )

            assert(result.next().isNo && !result.hasNext())
        }

        /** Testing Solve With Timeout FIX**/
        @Test
        @Throws(Exception::class)
        fun solveQueryWithTimeout() {
            val client = SimpleSolver.prolog.basicClient("""
                       p(X):-p(X).
                       """.trimIndent())
            val result = client.solve("p(X)", 10).iterator().next()
            assertContains(
                result.exception.toString(), "TimeOutException"
            )
        }

        /** Testing Solve List With Timeout FIX**/
        @Test
        @Throws(Exception::class)
        fun solveQueryAsListWithTimeout() {
            val client = SimpleSolver.prolog.basicClient("""
                       p(a).
                       p(X):-p(X).
                       """.trimIndent())
            val results = client.solveList("p(X)", SolveOptions.allEagerlyWithTimeout(50))
            assertEquals(
                Struct.of("p", Term.parse("a")), results.first().solvedQuery
            )
            assertContains(
                results.last().exception.toString(),"TimeOutException"
            )
        }
    */
}