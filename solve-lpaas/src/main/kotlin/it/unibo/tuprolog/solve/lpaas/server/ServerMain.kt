package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.ServerBuilder

fun main(/*args: Array<String>*/) {
    val server = ServerBuilder.forPort(8080)
        .addService(SolverService)
        .build()
    server.start()

    val server2 = ServerBuilder.forPort(8081)
        .addService(SolverFactoryService)
        .build()
    server2.start()

    println("Listening on port " + 8080)
    server.awaitTermination()
    Runtime.getRuntime().addShutdownHook(Thread { server.shutdownNow() })
}