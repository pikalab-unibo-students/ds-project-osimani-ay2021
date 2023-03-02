package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.util.*
import java.util.concurrent.TimeUnit

internal class ClientPrologSolverImpl(staticKb: String = DEFAULT_STATIC_THEORY, dynamicKb: String = ""):
    SimpleSolver {

    private var solverID: String

    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build()

    private val clientSolver: SolverGrpc.SolverFutureStub = SolverGrpc.newFutureStub(channel)

    init {
        val createSolverRequest: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(staticKb).setDynamicKb(dynamicKb).build()
        solverID = SolverFactoryGrpc.newFutureStub(channel).produceSolver(createSolverRequest).get().id
    }

    override fun solve(goal: String): SolutionsSequence {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setSolverID(solverID).setStruct(goal).build()
        val reply = clientSolver.solve(request).get()
        solverID = reply.solverID
        return SolutionsSequence(solverID, reply.computationID, reply.query, channel)
    }

    /*private fun buildRequestWithOptionsMessage(goal:String, options: SolveOptions): SolveRequestWithOptions {
        val request = SolveRequestWithOptions.newBuilder()
            .setId(solverId).setStruct(goal)
            .addOptions(buildOption(TIMEOUT_OPTION, options.timeout))
            .addOptions(buildOption(LIMIT_OPTION, options.limit.toLong()))
        if(options.isEager) request.addOptions(buildOption(EAGER_OPTION))
        if(options.isLazy) request.addOptions(buildOption(LAZY_OPTION))
        return request.build()
    }*/

    override fun closeClient() {
        channel.shutdown()
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }
}