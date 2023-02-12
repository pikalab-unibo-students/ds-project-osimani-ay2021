package it.unibo.tuprolog.solve.lpaas.client

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolveRequest
import it.unibo.tuprolog.solve.lpaas.SolveRequestWithTimeout
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import it.unibo.tuprolog.solve.lpaas.server.SolverService
import java.util.concurrent.TimeUnit

fun main(/*args: Array<String>*/) {
    val prova: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
        .setStruct("p(X)")
        .setTimeout(5).build()
    val prova2: SolveRequest = SolveRequest.newBuilder().setStruct("f(X)").build()

    val replyPrinter: StreamObserver<SolutionReply> = object : StreamObserver<SolutionReply> {
        override fun onNext(value: SolutionReply) { println(value.solution)}
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    val channel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    val client = SolverGrpc.newStub(channel)

    client.solveWithTimeout(prova, replyPrinter)
    channel.awaitTermination(1, TimeUnit.SECONDS)
    client.solve(prova2, replyPrinter)

    channel.shutdown()
    channel.awaitTermination(10, TimeUnit.SECONDS)
    Runtime.getRuntime().addShutdownHook(Thread { channel.shutdownNow() })
}