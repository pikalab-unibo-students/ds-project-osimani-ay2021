import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.libs.io.IOLib
import it.unibo.tuprolog.solve.libs.oop.OOPLib
import it.unibo.tuprolog.solve.lpaas.client.ClientMutableSolver
import it.unibo.tuprolog.solve.lpaas.client.ClientSolver
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.lpaas.util.DEFAULT_STATIC_THEORY
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.parse
import kotlin.test.*


class MutableSolverOperationsTest {
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
    fun useAssertA() {
        val previousTheory = clients[MUTABLE]!!.getDynamicKB()
        (clients[MUTABLE]!! as ClientMutableSolver)
            .assertA(Struct.of("p", Term.parse("a")))
        assertEquals(
            Theory.of(listOf(Clause.parse("p(a) :- true.")) + previousTheory.clauses),
            clients[MUTABLE]!!.getDynamicKB())
    }

    @Test
    @Throws(Exception::class)
        fun useAssertZ() {
        val previousTheory = clients[MUTABLE]!!.getDynamicKB()
        (clients[MUTABLE]!! as ClientMutableSolver)
            .assertZ(Struct.of("p", Term.parse("a")))
        assertEquals(
            Theory.of(previousTheory + Clause.parse("p(a) :- true.")),
            clients[MUTABLE]!!.getDynamicKB())
    }

    @Test
    @Throws(Exception::class)
    fun useLoadStaticKB() {
        val newTheory = Theory.of(
        Clause.parse("p(a) :- true."),
        Clause.parse("p(b) :- true."))
        (clients[MUTABLE]!! as ClientMutableSolver)
            .loadStaticKb(newTheory)
        assertEquals(
            newTheory,
            clients[MUTABLE]!!.getStaticKB())
    }

    @Test
    @Throws(Exception::class)
    fun useLoadLibrary() {
        val library = OOPLib.alias
        (clients[MUTABLE]!! as ClientMutableSolver)
            .unloadLibrary(library)
        assert(clients[MUTABLE]!!.getLibraries().contains(library))
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
    fun failureOfMutableMethods() {
        val solver = ClientSolverFactory.mutableSolverOf()
        solver.closeClient(true)
        assertFails { solver.loadLibrary("x") }
        assertFails { solver.unloadLibrary("x") }
        assertFails { solver.setRuntime(setOf()) }
        assertFails { solver.loadStaticKb(Theory.empty()) }
        assertFails { solver.appendStaticKb(Theory.empty()) }
        assertFails { solver.resetStaticKb() }
        assertFails { solver.loadDynamicKb(Theory.empty()) }
        assertFails { solver.appendDynamicKb(Theory.empty()) }
        assertFails { solver.resetDynamicKb() }
        val struct = Struct.of("f", Term.parse("a"))
        assertFails { solver.assertA(struct) }
        assertFails { solver.assertZ(struct) }
        assertFails { solver.retract(struct) }
        assertFails { solver.retractAll(struct) }
        assertFails { solver.setFlag("f", struct) }
        assertFails { solver.setStandardInput("hello") }
        //These should print the error
        solver.setStandardOutput(OutputChannel.stdOut())
        solver.setStandardError(OutputChannel.stdOut())
        solver.setWarnings(OutputChannel.stdOut())
    }
}
