import it.unibo.tuprolog.solve.SolverFactory
import it.unibo.tuprolog.solve.TestAssertA
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import kotlin.test.*

class TestClientAssertA : TestAssertA, SolverFactory by TrasparentFactory {
    private val prototype = TestAssertA.prototype(this)
    @Test
    override fun testAssertAClause() {
        prototype.testAssertAClause()
    }
    @Ignore
    @Test
    override fun testAssertAAny() {
        prototype.testAssertAAny()
    }
    @Ignore
    @Test
    override fun testAssertANumber() {
        prototype.testAssertANumber()
    }
    @Ignore
    @Test
    override fun testAssertAFooNumber() {
        prototype.testAssertAFooNumber()
    }
    @Ignore
    @Test
    override fun testAssertAAtomTrue() {
        prototype.testAssertAAtomTrue()
    }

}
