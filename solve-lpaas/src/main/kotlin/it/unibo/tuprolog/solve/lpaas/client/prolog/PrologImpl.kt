package it.unibo.tuprolog.solve.lpaas.client.prolog

import io.grpc.ManagedChannelBuilder
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.client.SimpleSolver
import it.unibo.tuprolog.solve.lpaas.util.*

internal class ClientPrologSolverImpl(staticKb: String = DEFAULT_STATIC_THEORY, dynamicKb: String = ""):
    SimpleSolver {

    private var solverId = ""

    private val clientSolverFactory: SolverFactoryGrpc.SolverFactoryFutureStub =
        SolverFactoryGrpc.newFutureStub(ManagedChannelBuilder.forAddress("localhost", 8081)
            .usePlaintext()
            .build())
    private val clientSolver: SolverGrpc.SolverFutureStub =
        SolverGrpc.newFutureStub(ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build())


    init {
        val createSolver: SolverRequest = SolverRequest.newBuilder()
            .setStaticKb(staticKb).setDynamicKb(dynamicKb).build()
        solverId = clientSolverFactory.produceSolver(createSolver).get().id
    }

    override fun solve(goal: String): SolutionSequence {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(solverId).setStruct(goal).build()
        clientSolver.solve(request).get()
        return SolutionSequence(solverId, goal)

    }

    override fun solve(goal: String, timeout: Long): SolutionSequence {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(solverId).setTimeout(timeout).setStruct(goal).build()
        clientSolver.solveWithTimeout(request).get()
        return SolutionSequence(solverId, goal)
    }

    override fun solve(goal: String, options: SolveOptions): SolutionSequence {
        clientSolver.solveWithOptions(buildRequestWithOptionsMessage(goal, options))
        return SolutionSequence(solverId, goal)
    }

    override fun solveOnce(goal: String): SolutionSequence {
        val request: SolveRequest = SolveRequest.newBuilder()
            .setId(solverId).setStruct(goal).build()
        clientSolver.solveOnce(request)
        return SolutionSequence(solverId, goal)
    }

    override fun solveOnce(goal: String, timeout: Long): SolutionSequence {
        val request: SolveRequestWithTimeout = SolveRequestWithTimeout.newBuilder()
            .setId(solverId).setTimeout(timeout).setStruct(goal).build()
        clientSolver.solveOnceWithTimeout(request)
        return SolutionSequence(solverId, goal)
    }

    override fun solveOnce(goal: String, options: SolveOptions): SolutionSequence {
        clientSolver.solveOnceWithOptions(buildRequestWithOptionsMessage(goal, options))
        return SolutionSequence(solverId, goal)
    }

    override fun solveList(goal: String): List<Solution> {
        return solve(goal).toList()
    }

    override fun solveList(goal: String, timeout: Long): List<Solution> {
        return solve(goal, timeout).toList()
    }

    override fun solveList(goal: String, options: SolveOptions): List<Solution> {
        return solve(goal, options).toList()
    }

    private fun buildOption(name: String, value: Long = -1): Options {
        return Options.newBuilder().setName(name).setValue(value).build()
    }

    private fun buildRequestWithOptionsMessage(goal:String, options: SolveOptions): SolveRequestWithOptions {
        val request = SolveRequestWithOptions.newBuilder()
            .setId(solverId).setStruct(goal)
            .addOptions(buildOption(TIMEOUT_OPTION, options.timeout))
            .addOptions(buildOption(LIMIT_OPTION, options.limit.toLong()))
        if(options.isEager) request.addOptions(buildOption(EAGER_OPTION))
        if(options.isLazy) request.addOptions(buildOption(LAZY_OPTION))
        return request.build()
    }
}