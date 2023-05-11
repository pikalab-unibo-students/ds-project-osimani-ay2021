import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.examples.*
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.InputStore
import it.unibo.tuprolog.solve.library.Runtime
import kotlin.test.*

class TestBasicPrimitives {

    private lateinit var solver: Solver

    /*private val servers: MutableSet<Thread> = mutableSetOf()

    init {
        var port = 8080
        listOf(innestedPrimitive, ntPrimitive, readerPrimitive, throwablePrimitive, writerPrimitive).forEach {
            val thread = Thread {
                PrimitiveServerFactory
                    .startService(it, port, "customLibrary")
            }
            servers.add(thread)
            thread.start()
            Thread.sleep(1000)
            port++
        }
    }*/

    @BeforeTest
    fun beforeEach() {
        logicProgramming {
            solver = Solver.prolog.solverWithDefaultBuiltins(
                otherLibraries = Runtime.of(
                    PrimitiveClientFactory.searchLibrary("customLibrary")
                ),
                stdIn = InputChannel.of("hello")
            )
        }
    }

    /** Testing Basic Primitive **/
    @Test
    @Throws(Exception::class)
    fun testNt() {
        logicProgramming {
            val query = "nt"(X)
            val solutions = solver.solve(query).take(4).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "nt"(0),
                    "nt"(1),
                    "nt"(2),
                    "nt"(3),
                ).toList(),
                solutions
            )
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testSubSolve() {
        logicProgramming {
            val query = "solve"("nt"(X))
            val solutions = solver.solve(query).take(2).map {
                it.solvedQuery!!
            }.toList()
            assertEquals(
                listOf(
                    "solve"("nt"(0)),
                    "solve"("nt"(1)),
                ).toList(),
                solutions
            )
        }
    }

    /** Testing SubSolve **/
    @Test
    @Throws(Exception::class)
    fun testReadLine() {
        val solutions = logicProgramming {
            val query = "readLine"(InputStore.STDIN, X)
            solver.solve(query).take(6).toList()
        }
        assertTrue { solutions.last().isNo }
        assertEquals(
            listOf(
                "h", "e", "l", "l", "o"
            ).toList(),
            solutions.take(5).map { it.substitution.values.first().toString()}
        )
    }
}