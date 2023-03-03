package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.TimeDuration
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.util.*
import it.unibo.tuprolog.utils.forceCast
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

    override fun closeClient() {
        channel.shutdown()
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

    override fun solve(goal: String): SolutionsSequence {
        return solve(goal, SolveOptions.DEFAULT)
    }

    override fun solve(goal: String, timeout: TimeDuration): SolutionsSequence {
        return solve(goal, SolveOptions.allLazilyWithTimeout(timeout))
    }

    override fun solve(goal: String, options: SolveOptions): SolutionsSequence {
        val reply = clientSolver.solve(buildRequestWithOptionsMessage(goal, options)).get()
        solverID = reply.solverID
        return SolutionsSequence(solverID, reply.computationID, reply.query, channel)
    }

    override fun solveList(goal: String): List<Solution> {
        return solve(goal).asSequence().toList()
    }

    override fun solveList(goal: String, timeout: TimeDuration): List<Solution> {
        return solve(goal, timeout).asSequence().toList()
    }

    override fun solveList(goal: String, options: SolveOptions): List<Solution> {
        return solve(goal, options).asSequence().toList()
    }

    override fun solveOnce(goal: String): Solution {
        return solve(goal, SolveOptions.someLazily(1)).getSolution(0)
    }

    override fun solveOnce(goal: String, timeout: TimeDuration): Solution {
        return solve(goal, SolveOptions.someLazilyWithTimeout(1, timeout)).getSolution(0)
    }

    override fun solveOnce(goal: String, options: SolveOptions): Solution {
        return solve(goal, options.setLimit(1)).getSolution(0)
    }

    private fun buildRequestWithOptionsMessage(goal:String, options: SolveOptions): SolveRequest {
        val request = SolveRequest.newBuilder()
            .setSolverID(solverID).setStruct(goal)
            .addOptions(buildOption(TIMEOUT_OPTION, options.timeout))
            .addOptions(buildOption(LIMIT_OPTION, options.limit.toLong()))
        if(options.isEager) request.addOptions(buildOption(EAGER_OPTION))
        if(options.isLazy) request.addOptions(buildOption(LAZY_OPTION))
        //options.customOptions.forEach { request.addOptions(buildOption(it.key, (it.value))) }
        return request.build()
    }

    private fun buildOption(key: String, value: Long = -1): Options {
        return Options.newBuilder().setName(key).setValue(value).build()
    }
}