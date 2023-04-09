import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.format
import it.unibo.tuprolog.dsl.theory.logicProgramming
import it.unibo.tuprolog.solve.*
import it.unibo.tuprolog.solve.lpaas.client.prolog.ClientSolverFactory
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import it.unibo.tuprolog.solve.lpaas.server.Service
import kotlin.test.*

class TestServerSolver : TestSolver, SolverFactory by TrasparentFactory {

    private val prototype = Signature("ensure_executable", 1).let {
        TestSolver.prototype(this, it, it, it)
    }

    private val service = Service()
    @BeforeTest
    fun before() = service.start()
    @AfterTest
    fun after() = service.stop(true)

    override val callErrorSignature: Signature
        get() = prototype.callErrorSignature

    override val nafErrorSignature: Signature
        get() = prototype.nafErrorSignature

    override val notErrorSignature: Signature
        get() = prototype.notErrorSignature

    @Test
    override fun testUnknownFlag2() {
        prototype.testUnknownFlag2()
    }

    @Test
    @Ignore
    override fun testUnknownFlag1() {
        prototype.testUnknownFlag1()
    }

    @Test
    @Ignore
    override fun testSideEffectsPersistentAfterBacktracking1() {
        prototype.testSideEffectsPersistentAfterBacktracking1()
    }

    @Test
    @Ignore
    override fun testFindAll() {
        prototype.testFindAll()
    }

    @Test
    override fun testAssert() {
        prototype.testAssert()
    }

    @Test
    override fun testAssertZ() {
        prototype.testAssertZ()
    }

    @Test
    override fun testAssertA() {
        prototype.testAssertA()
    }

    @Test
    override fun testWrite() {
        val outputs = mutableListOf<String>()
        logicProgramming {
            val solver = ClientSolverFactory.solverOf(defaultBuiltins = true)

            val terms = ktListOf(
                atomOf("atom"),
                atomOf("a string"),
                numOf(1),
                numOf(2.1),
                "f"("x")
            )

            val query = tupleOf(terms.map { write(it) }.append(nl))

            val solutions = solver.solve(query, SolveOptions.allEagerlyWithTimeout(mediumDuration))
                .asSequence().toList()

            assertSolutionEquals(
                ktListOf(query.yes()),
                solutions
            )

            outputs.addAll(solver.getOutputChannels()["\$current"]!!)

            assertEquals(
                terms.map { it.format(TermFormatter.default()) }.append("\n"),
                outputs
            )
            solver.closeClient()
        }
    }

    @Test
    @Ignore
    override fun testStandardOutput() {
        prototype.testStandardOutput()
    }

    @Test
    override fun testTrue() {
        prototype.testTrue()
    }

    @Test
    override fun testIfThen1() {
        prototype.testIfThen1()
    }

    @Test
    override fun testIfThen2() {
        prototype.testIfThen2()
    }

    @Test
    override fun testIfThenElse1() {
        prototype.testIfThenElse1()
    }

    @Test
    override fun testIfThenElse2() {
        prototype.testIfThenElse2()
    }

    @Test
    @Ignore
    override fun testTimeout1() {
        prototype.testTimeout1()
    }

    @Test
    @Ignore
    override fun testTimeout2() {
        prototype.testTimeout2()
    }

    @Test
    @Ignore
    override fun testTimeout3() {
        prototype.testTimeout3()
    }

    @Test
    override fun testTimeout4() {
        prototype.testTimeout4()
    }

    @Test
    override fun testUnification() {
        prototype.testUnification()
    }

    @Test
    override fun testSimpleCutAlternatives() {
        prototype.testSimpleCutAlternatives()
    }

    @Test
    override fun testCutAndConjunction() {
        prototype.testCutAndConjunction()
    }

    @Test
    override fun testCutConjunctionAndBacktracking() {
        prototype.testCutConjunctionAndBacktracking()
    }

    @Test
    @Ignore
    override fun testMaxDurationParameterAndTimeOutException() {
        prototype.testMaxDurationParameterAndTimeOutException()
    }

    @Test
    override fun testPrologStandardSearchTreeExample() {
        prototype.testPrologStandardSearchTreeExample()
    }

    @Test
    override fun testPrologStandardSearchTreeWithCutExample() {
        prototype.testPrologStandardSearchTreeWithCutExample()
    }

    @Test
    override fun testBacktrackingWithCustomReverseListImplementation() {
        prototype.testBacktrackingWithCustomReverseListImplementation()
    }

    @Test
    override fun testWithPrologStandardConjunctionExamples() {
        prototype.testWithPrologStandardConjunctionExamples()
    }

    @Test
    @Ignore
    override fun testConjunctionProperties() {
        prototype.testConjunctionProperties()
    }

    @Ignore
    @Test
    override fun testCallPrimitive() {
        prototype.testCallPrimitive()
    }

    @Ignore
    @Test
    override fun testCallPrimitiveTransparency() {
        prototype.testCallPrimitiveTransparency()
    }

    @Ignore
    @Test
    override fun testCatchPrimitive() {
        prototype.testCatchPrimitive()
    }

    @Ignore
    @Test
    override fun testCatchPrimitiveTransparency() {
        prototype.testCatchPrimitiveTransparency()
    }

    @Ignore
    @Test
    override fun testHaltPrimitive() {
        prototype.testHaltPrimitive()
    }

    @Ignore
    @Test
    override fun testNotPrimitive() {
        prototype.testNotPrimitive()
    }

    @Ignore
    @Test
    override fun testNotModularity() {
        prototype.testNotModularity()
    }

    @Test
    override fun testIfThenRule() {
        prototype.testIfThenRule()
    }

    @Test
    override fun testIfThenElseRule() {
        prototype.testIfThenElseRule()
    }

    @Ignore
    @Test
    override fun testNumbersRangeListGeneration() {
        prototype.testNumbersRangeListGeneration()
    }

    @Test
    override fun testConjunction() {
        prototype.testConjunction()
    }

    @Test
    override fun testConjunctionWithUnification() {
        prototype.testConjunctionWithUnification()
    }

    @Test
    override fun testBuiltinApi() {
        prototype.testBuiltinApi()
    }

    @Test
    override fun testDisjunction() {
        prototype.testDisjunction()
    }

    @Test
    override fun testFailure() {
        prototype.testFailure()
    }

    @Test
    override fun testDisjunctionWithUnification() {
        prototype.testDisjunctionWithUnification()
    }

    @Test
    override fun testConjunctionOfConjunctions() {
        prototype.testConjunctionOfConjunctions()
    }

    @Test
    override fun testMember() {
        prototype.testMember()
    }

    @Test
    override fun testBasicBacktracking1() {
        prototype.testBasicBacktracking1()
    }

    @Test
    override fun testBasicBacktracking2() {
        prototype.testBasicBacktracking2()
    }

    @Test
    override fun testBasicBacktracking3() {
        prototype.testBasicBacktracking3()
    }

    @Test
    override fun testBasicBacktracking4() {
        prototype.testBasicBacktracking4()
    }

    @Test
    override fun testAssertRules() {
        prototype.testAssertRules()
    }

    @Test
    override fun testRetract() {
        prototype.testRetract()
    }

    @Test
    override fun testNatural() {
        prototype.testNatural()
    }

    @Test
    @Ignore
    override fun testFunctor() {
        prototype.testFunctor()
    }

    @Test
    @Ignore
    override fun testUniv() {
        prototype.testUniv()
    }

    @Test
    override fun testRetractAll() {
        prototype.testRetractAll()
    }

    @Test
    override fun testAppend() {
        prototype.testAppend()
    }

    @Test
    override fun testTermGreaterThan() {
        prototype.testTermGreaterThan()
    }

    @Test
    override fun testTermSame() {
        prototype.testTermSame()
    }

    @Test
    override fun testTermLowerThan() {
        prototype.testTermLowerThan()
    }

    @Test
    override fun testTermGreaterThanOrEqual() {
        prototype.testTermGreaterThanOrEqual()
    }

    @Test
    override fun testTermLowerThanOrEqual() {
        prototype.testTermLowerThanOrEqual()
    }

    @Test
    override fun testTermNotSame() {
        prototype.testTermNotSame()
    }
}
