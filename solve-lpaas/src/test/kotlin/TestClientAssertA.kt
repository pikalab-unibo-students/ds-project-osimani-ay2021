import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.exception.error.DomainError
import it.unibo.tuprolog.solve.exception.error.InstantiationError
import it.unibo.tuprolog.solve.exception.error.PermissionError
import it.unibo.tuprolog.solve.exception.error.TypeError
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import kotlin.test.*

class TestClientAssertA : TestAssertA, SolverFactory by TrasparentFactory {
    private val prototype = TestAssertA.prototype(this)

    private val service = Service()
    @BeforeTest
    fun before() = service.start()
    @AfterTest
    fun after() = service.stop(true)

    @Test
    override fun testAssertAClause() {
        prototype.testAssertAClause()
    }

    @Test
    override fun testAssertAAny() {
        logicProgramming {
            val solver = TrasparentFactory.solverWithDefaultBuiltins()

            val query = asserta(`_`)
            val solutions = solver.solve(query, mediumDuration).toList()

            assertTrue(
                solutions.first().exception!!.message!!.contains(
                    query.halt(
                        InstantiationError.forArgument(
                            DummyInstances.executionContext,
                            Signature("asserta", 1),
                            `_`,
                            index = 0
                        )
                    ).exception.toString())
            )
        }
    }
    @Test
    override fun testAssertANumber() {
        logicProgramming {
            val solver = TrasparentFactory.solverWithDefaultBuiltins()

            val query = asserta(4)
            val solutions = solver.solve(query, mediumDuration).toList()

            assertTrue(
                solutions.first().exception!!.message!!.contains(
                query.halt(
                    TypeError.forArgument(
                        DummyInstances.executionContext,
                        Signature("asserta", 1),
                        TypeError.Expected.CALLABLE,
                        numOf(4),
                        index = 0
                    )).exception.toString()
                )
            )
        }
    }
    @Test
    override fun testAssertAFooNumber() {
        logicProgramming {
            val solver = TrasparentFactory.solverWithDefaultBuiltins()

            val query = asserta("foo" `if` 4)
            val solutions = solver.solve(query, mediumDuration).toList()

            assertTrue(
                solutions.first().exception!!.message!!.contains(
                    query.halt(
                        DomainError.forArgument(
                            DummyInstances.executionContext,
                            Signature("asserta", 1),
                            DomainError.Expected.CLAUSE,
                            ("foo" `if` 4),
                            index = 0
                        )).exception.toString()
                )
            )
        }
    }

    @Test
    override fun testAssertAAtomTrue() {
        logicProgramming {
            val solver = TrasparentFactory.solverWithDefaultBuiltins()

            val query = asserta(atom(`_`) `if` true)
            val solutions = solver.solve(query, mediumDuration).toList()

            assertTrue(
                solutions.first().exception!!.message!!.contains(
                    query.halt(
                        PermissionError.of(
                            DummyInstances.executionContext,
                            Signature("asserta", 1),
                            PermissionError.Operation.MODIFY,
                            PermissionError.Permission.PRIVATE_PROCEDURE,
                            "atom" / 1
                        )
                    ).exception.toString()
                )
            )
        }
    }
}
