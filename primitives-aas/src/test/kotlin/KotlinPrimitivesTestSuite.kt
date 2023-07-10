import io.grpc.Server
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.CountDownLatch
import kotlin.test.*

abstract class KotlinPrimitivesTestSuite: AbstractPrimitivesTestSuite() {

    private val activeServices = mutableMapOf<Int, Server>()
    abstract val primitives: List<DistributedPrimitiveWrapper>

    override fun getActivePorts(): Set<Int> = activeServices.keys

    @BeforeTest
    override fun beforeEach() {
        var port = 8080
        val latch = CountDownLatch(primitives.size)
        primitives.forEach {
            executor.submit {
                val service = PrimitiveServerFactory.startService(it, port, "customLibrary")
                latch.countDown()
                activeServices[port] = service
                service.awaitTermination()
            }
            Thread.sleep(3000)
            port++
        }
        latch.await()
        super.beforeEach()
    }

    @AfterTest
    override fun afterEach() {
        activeServices.values.forEach {
            it.shutdownNow()
        }
        activeServices.clear()
        super.afterEach()
    }
}