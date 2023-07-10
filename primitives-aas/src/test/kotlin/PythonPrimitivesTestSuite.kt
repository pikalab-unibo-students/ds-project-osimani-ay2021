import java.util.concurrent.CompletableFuture

abstract class PythonPrimitivesTestSuite: AbstractPrimitivesTestSuite() {

    private val numOfPrimitives: Int = 23
    private val startingPort = 8080
    private val maxPort = startingPort + numOfPrimitives
    private var serverProcess: Process? = null

    override fun getActivePorts(): Set<Int> =
        (startingPort until maxPort).toSet()


    init {
        val p1 = ProcessBuilder("cmd.exe", "/c", "pip",
            "install", "--upgrade", "prolog-primitives").inheritIO().start()
        p1.errorStream.bufferedReader().useLines {
            it.forEach { line -> println(line) }
        }
        p1.waitFor()
    }

    private val logger = mutableListOf<String>()
    private var inputThread: Thread = Thread()
    private var errorThread: Thread = Thread()

    override fun beforeEach() {
        println("starting to listen")
        serverProcess = ProcessBuilder(
            "python", {}.javaClass.getResource("/Untitled-1.py")!!.path.drop(1)
        ).start()
        inputThread = Thread {
            serverProcess!!.inputStream.bufferedReader().useLines {
                it.forEach { line -> println(line) }
            }
        }
        errorThread = Thread {
            serverProcess!!.errorStream.bufferedReader().useLines {
                logger.addAll(it.toList())
            }
        }
        inputThread.start()
        errorThread.start()
        Thread.sleep(7000)
        println("Server Started")
        super.beforeEach()
    }

    override fun afterEach() {
        super.afterEach()
        println(logger)
        serverProcess!!.destroyForcibly()
        inputThread.interrupt()
        errorThread.interrupt()
    }
}