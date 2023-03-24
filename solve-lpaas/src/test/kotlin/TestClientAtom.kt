import it.unibo.tuprolog.solve.lpaas.server.Service
import it.unibo.tuprolog.solve.SolverFactory
import it.unibo.tuprolog.solve.TestAtom
import it.unibo.tuprolog.solve.lpaas.client.trasparent.TrasparentFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestClientAtom : TestAtom, SolverFactory by TrasparentFactory {
    private val prototype = TestAtom.prototype(this)

    private val service = Service()
    @BeforeTest
    fun before() = service.start()
    @AfterTest
    fun after() = service.stop()

    @Test
    override fun testAtomAtom() {
        prototype.testAtomAtom()
    }

    @Test
    override fun testAtomString() {
        prototype.testAtomString()
    }

    @Test
    override fun testAtomAofB() {
        prototype.testAtomAofB()
    }

    @Test
    override fun testAtomVar() {
        prototype.testAtomVar()
    }

    @Test
    override fun testAtomEmptyList() {
        prototype.testAtomEmptyList()
    }

    @Test
    override fun testAtomNum() {
        prototype.testAtomNum()
    }

    @Test
    override fun testAtomNumDec() {
        prototype.testAtomNumDec()
    }
}
