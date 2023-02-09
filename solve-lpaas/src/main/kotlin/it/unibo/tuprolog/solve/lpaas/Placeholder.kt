package it.unibo.tuprolog.solve.lpaas

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.util.concurrent.TimeUnit

fun main(/*args: Array<String>*/) {
    val server = ServerBuilder.forPort(8080)
        .addService(SolverService)
        .build()
    server.start()

    val prova: SolveRequest = SolveRequest.newBuilder().setStruct("bla").build()

    val replyPrinter: StreamObserver<SolutionReply> = object : StreamObserver<SolutionReply> {
        override fun onNext(value: SolutionReply) {
            println(value.solution)
        }

        override fun onError(t: Throwable) {
            t.printStackTrace()
        }

        override fun onCompleted() {
            // do nothing
        }
    }

    val channel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    val client = SolverGrpc.newStub(channel)

    client.solve(prova, replyPrinter)

    channel.shutdown()
    channel.awaitTermination(1, TimeUnit.SECONDS)

    println("Listening on port " + 8080)
    server.awaitTermination()
    Runtime.getRuntime().addShutdownHook(Thread { server.shutdownNow() })
}