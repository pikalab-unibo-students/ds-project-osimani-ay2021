import io.grpc.internal.testing.StreamRecorder
import it.unibo.tuprolog.solve.lpaas.SolutionListReply
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolverImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals

class SolverTest {

    private val client1: ClientSolver = ClientSolverImpl()
    private val client2: ClientSolver = ClientSolverImpl()

    /*@Throws(IOException::class)
    @BeforeTest
    fun beforeEach() {
    }*/

    /** Testing Simple Solve **/
    @Test
    @Throws(Exception::class)
    fun simpleSolveQuery() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.solveQuery("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)", "f(d)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing Factory of Solvers **/
    @Test
    @Throws(Exception::class)
    fun createSolver() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a).
                   p(c).
                   """.trimIndent())
        client1.solveQuery("p(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("p(a)", "p(c)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing async-nature of requests **/
    @Test
    @Throws(Exception::class)
    fun asyncRequests() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(X):-p(X).
                   """.trimIndent())
        client1.solveQuery("p(X)")
        client2.solveQuery("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)", "f(d)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing Stream-Like Nature of Solve **/
    @Test
    @Throws(Exception::class)
    fun testStreamLikeResponse() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a).
                   p(c) :- sleep(3000).
                   """.trimIndent())
        client1.solveQuery("p(X)", responseStream)
        runBlocking {
            delay(1000)
        }
        assertContentEquals(
            listOf("p(a)"),
            responseStream.values.map { it.solvedQuery }
        )
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("p(a)", "p(c)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing SolveAsList **/
    @Test
    @Throws(Exception::class)
    fun solveQueryList() {
        val responseStream: StreamRecorder<SolutionListReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a) :- sleep(3000).
                   p(c).
                   """.trimIndent())
        client1.solveQueryAsList("p(X)", responseStream)
        runBlocking {
            delay(1000)
        }
        assert(responseStream.values.isEmpty())
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("p(a)", "p(c)"),
            responseStream.values[0].solutionList.map {
                println(it.solvedQuery)
                it.solvedQuery
            }
        )
    }

    /** Testing Solve Once **/
    @Test
    @Throws(Exception::class)
    fun solveOnceQuery() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.solveQueryOnce("f(X)", responseStream)
        responseStream.awaitCompletion()
        assertContentEquals(
            listOf("f(b)"),
            responseStream.values.map { it.solvedQuery }
        )
    }

    /** Testing Solve With Timeout **/
    @Test
    @Throws(Exception::class)
    fun solveQueryWithTimeout() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.createSolver("""
                   p(a).
                   p(X):-p(X).
                   """.trimIndent())
        client1.solveWithTimeout("p(X)", 100, responseStream)
        responseStream.awaitCompletion()
        val results = responseStream.values.map {
            Pair(it.solvedQuery, it.error)
        }
        assertContains(
            results.map { it.first }, "p(a)"
        )
        assertContains(
            results.last().second, "TimeOutException"
        )
    }

    /** Testing Solve With Lazy Option **/
    @Test
    @Throws(Exception::class)
    fun solveLazyQuery() {
        val responseStream: StreamRecorder<SolutionReply> = StreamRecorder.create()
        client1.requestQueryWithOptions("f(X)",
            laziness = true,
            callback = responseStream)
        responseStream.awaitCompletion()
        println(responseStream.values.size)
        assertContentEquals(
            listOf("f(b)", "f(d)"),
            responseStream.values.map { it.solvedQuery }
        )
    }
}


