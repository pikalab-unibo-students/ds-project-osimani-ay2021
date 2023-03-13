package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.Server
import io.grpc.ServerBuilder
import java.io.IOException


/** TO-DO
 * Add stream-like listeners to input/output channels
 * Add generic deque for warnings when adding channel
 * Fix error when testing mutables
 */

class Service {

    private var serviceSolver: Server? = null

    fun start() {
        serviceSolver = ServerBuilder.forPort(8080)
            .addService(SolverService)
            .addService(MutableSolverService)
            .addService(SolverFactoryService)
            .build()
        serviceSolver!!.start()
    }

    @Throws(InterruptedException::class)
    fun stop() {
        serviceSolver!!.shutdown()
        this.awaitTermination()
    }

    fun awaitTermination() {
        serviceSolver!!.awaitTermination()
    }

    fun shutdownNow() {
        serviceSolver!!.shutdownNow()
    }

}

fun main() {
    val service = Service()
    service.start()
    println("Listening on port " + 8080)
    service.awaitTermination()
    Runtime.getRuntime().addShutdownHook( Thread { service.shutdownNow() })
}
