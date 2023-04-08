package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.Server
import io.grpc.ServerBuilder
import it.unibo.tuprolog.solve.lpaas.server.database.DbManager
import it.unibo.tuprolog.solve.lpaas.server.services.MutableSolverService
import it.unibo.tuprolog.solve.lpaas.server.services.SolverFactoryService
import it.unibo.tuprolog.solve.lpaas.server.services.SolverService


/** TO-DO
 * CustomStore implementation from solution
 * ?
 * Handling of stream-like outputs channels
 * Handling of generic Stream-Observer
 * Operators for initialization of solver
 */

/**
 * - Reimplement channels deque collectors as extensions of inputchannelabstract ecc. DONE
 * - Put neutral element to close stream from server after closing channel DONE?
 * - Fix name in setStandardInput DONE
 * - Che senso ha cambiare stdErr e stdOut da client
 * - Change generic handling client-side DONE
 * - Implement Solver Interface client-side creating a different extension of extisting ..... DONE
 */

class Service(private val port: Int = 8080) {

    private var serviceSolver: Server? = null

    fun start() {
        DbManager.init(DbManager.URL_LOCAL)
        serviceSolver = ServerBuilder.forPort(port)
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

    companion object {
        fun main(args: Array<String>) {
            val port =
                try {
                    args.asSequence().filter { it.startsWith("port:") }.first().removePrefix("port:").toInt()
                } catch (e: Exception) {
                    8080
                }
            val service = Service(port)
            service.start()
            println("Listening on port $port")
            service.awaitTermination()
            Runtime.getRuntime().addShutdownHook(Thread { service.shutdownNow() })
        }
    }

}
