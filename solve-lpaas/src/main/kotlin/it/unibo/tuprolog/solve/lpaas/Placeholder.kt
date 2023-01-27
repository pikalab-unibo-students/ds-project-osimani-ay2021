package it.unibo.tuprolog.solve.lpaas

import io.grpc.ServerBuilder


// TODO this file is just a placeholder, and it can be removed

fun main(args: Array<String>) {
    val server = ServerBuilder.forPort(8080)
        .addService(MyGreetingService)
        .build()

    server.start()
}