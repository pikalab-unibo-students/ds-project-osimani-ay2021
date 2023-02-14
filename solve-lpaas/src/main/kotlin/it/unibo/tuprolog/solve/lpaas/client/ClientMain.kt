package it.unibo.tuprolog.solve.lpaas.client

import io.grpc.ManagedChannelBuilder
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.SolutionReply
import it.unibo.tuprolog.solve.lpaas.SolveRequest
import it.unibo.tuprolog.solve.lpaas.SolveRequestWithTimeout
import it.unibo.tuprolog.solve.lpaas.SolverFactoryGrpc
import it.unibo.tuprolog.solve.lpaas.SolverGrpc
import it.unibo.tuprolog.solve.lpaas.SolverReply
import it.unibo.tuprolog.solve.lpaas.SolverRequest
import it.unibo.tuprolog.solve.lpaas.server.SolverService
import java.util.concurrent.TimeUnit

fun main(/*args: Array<String>*/) {
    val replyPrinter: StreamObserver<SolutionReply> = object : StreamObserver<SolutionReply> {
        override fun onNext(value: SolutionReply) { println(value.solution)}
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    val replyGetter: StreamObserver<SolverReply> = object : StreamObserver<SolverReply> {
        override fun onNext(value: SolverReply) {
            val useSolver: SolveRequest = SolveRequest.newBuilder().setId(value.id).setStruct("p(X)").build()
            val channel2 = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build()
            val client = SolverGrpc.newStub(channel2)
            client.solve(useSolver, replyPrinter)
            channel2.awaitTermination(4, TimeUnit.SECONDS)
            channel2.shutdown()
        }
        override fun onError(t: Throwable) { t.printStackTrace() }
        override fun onCompleted() {}
    }

    val createSolver: SolverRequest = SolverRequest.newBuilder()
        .setStaticKb("""
                p(a).
                p(c).
                """.trimIndent()).build()
    val channel = ManagedChannelBuilder.forAddress("localhost", 8081)
        .usePlaintext()
        .build()
    val clientSolverFactory = SolverFactoryGrpc.newStub(channel)
    clientSolverFactory.produceSolver(createSolver, replyGetter)
    channel.awaitTermination(10, TimeUnit.SECONDS)
    channel.shutdown()

    Runtime.getRuntime().addShutdownHook(Thread { channel.shutdownNow() })
}