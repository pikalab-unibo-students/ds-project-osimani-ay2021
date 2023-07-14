import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture

abstract class PythonPrimitivesTestSuite: AbstractPrimitivesTestSuite() {

    private val numOfPrimitives: Int = 27
    private val startingPort = 8080
    private val maxPort = startingPort + numOfPrimitives
    private var serverProcess: Process? = null

    override fun getActivePorts(): Set<Int> =
        (startingPort until maxPort).toSet()


    init {
        println("install last version of library")
        val p = ProcessBuilder("cmd.exe", "/c", "pip",
            "install", "--upgrade", "prolog-primitives").inheritIO().start()
        p.inputStream.bufferedReader().useLines {
            it.forEach { line -> println(line) }
        }
        p.errorStream.bufferedReader().useLines {
            it.forEach { line -> println(line) }
        }
        p.waitFor()
    }

    override fun beforeEach() {
        println("starting to listen")
        serverProcess = ProcessBuilder(
            "python", {}.javaClass.getResource("/Untitled-1.py")!!.path.drop(1)
        ).start()
        executor.submit {
            serverProcess!!.inputStream.bufferedReader().useLines {
                it.forEach { line ->
                    println(line)
                }
            }
        }
        executor.submit  {
            serverProcess!!.errorStream.bufferedReader().useLines {
                log.addAll(it.toList())
            }
        }

        Thread.sleep(9000)
        println("Server Started")
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()
        println(log)
        serverProcess!!.destroyForcibly()
    }
}