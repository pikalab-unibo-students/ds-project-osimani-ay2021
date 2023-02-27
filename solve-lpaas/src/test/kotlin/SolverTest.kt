import io.grpc.internal.testing.StreamRecorder
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.SolutionListReply
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientPrologSolverImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SolverTest {

    private val basicSolver: SimpleSolver = SimpleSolver.prolog.basicClient()

    /*@Throws(IOException::class)
    @BeforeTest
    fun beforeEach() {
    }*/

    /** Testing Simple Solve **/
    @Test
    @Throws(Exception::class)
    fun simpleSolveQuery() {
        val result = basicSolver.solve("f(X)").iterator()
        assertEquals(
            Struct.of("f", Term.parse("b")),
            result.next().solvedQuery
        )
        assertEquals(
            Struct.of("f", Term.parse("d")),
            result.next().solvedQuery
        )
    }

    /** Testing Factory of Solvers **/
    @Test
    @Throws(Exception::class)
    fun createSolver() {
        val client = SimpleSolver.prolog.basicClient("""
                   p(a, b).
                   """.trimIndent())
        val result = client.solve("p(X, Y)").iterator()
        assertEquals(
            Struct.of("p", Term.parse("a"), Term.parse("b")),
            result.next().solvedQuery
        )
    }

    /** Testing Stream-Like Nature of Solve **/
    @Test
    @Throws(Exception::class)
    fun testStreamLikeResponse() {
        val result = basicSolver.solve("f(X)").iterator()
        assertEquals(
            Struct.of("f", Term.parse("b")),
            result.next().solvedQuery
        )

        assertEquals(
            Struct.of("f", Term.parse("d")),
            result.next().solvedQuery
        )

        assert(result.next().isNo && !result.hasNext())
    }

    /** Testing async-nature of requests FIX **/
    @Test
    @Throws(Exception::class)
    fun asyncRequests() {
        val client = SimpleSolver.prolog.basicClient("""
                   p(X):-p(X).
                   """.trimIndent())
        client.solve("p(X)").iterator()
        val result = basicSolver.solve("f(X)").iterator()
        assertEquals(
            Struct.of("f", Term.parse("b")),
            result.next().solvedQuery
        )
    }

    /** Testing SolveAsList FIX**/
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
}


