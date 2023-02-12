package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.ServerBuilder
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solver

fun main(/*args: Array<String>*/) {
    val server = ServerBuilder.forPort(8080)
        .addService(SolverService)
        .build()
    server.start()

    println("Listening on port " + 8080)
    server.awaitTermination()
    Runtime.getRuntime().addShutdownHook(Thread { server.shutdownNow() })
}