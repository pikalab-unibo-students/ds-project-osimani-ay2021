import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

abstract class PythonPrimitivesTestSuite: AbstractPrimitivesTestSuite() {

    private val numOfPrimitives: Int = 27
    private val startingPort = 8080
    private val maxPort = startingPort + numOfPrimitives
    private lateinit var serverProcess: Process

    override fun getActivePorts(): Set<Int> =
        (startingPort until maxPort).toSet()

    private fun ExecutorService.pythonModuleExec(moduleName: String, healthCheck: String): Process {
        val process = ProcessBuilder("python", "-m", moduleName).start()
        Runtime.getRuntime().addShutdownHook(Thread { 
            if (process.isAlive) {
                process.destroyForcibly()
                process.waitFor()
            }
        })
        val healthCheckPattern = healthCheck.toRegex()
        submit {
            process.errorStream.bufferedReader().useLines {
                it.forEach(System.err::println)
            }
        }
        val healthy = process.inputStream.bufferedReader().lineSequence().firstOrNull {
            it.matches(healthCheckPattern)
        }
        return if (healthy != null) process else error("Message here")
    }

    override fun beforeEach() {
        serverProcess = executor.pythonModuleExec("prolog_primitives.ml_lib", "^Servers listening from \\d+ to \\d+")
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()
        serverProcess.destroyForcibly()
        serverProcess.waitFor()
    }
}