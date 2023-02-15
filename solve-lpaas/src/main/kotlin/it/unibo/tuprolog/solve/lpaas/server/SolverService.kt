package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.*

object SolverService : SolverGrpc.SolverImplBase() {


    private val solvers = SolversCollection
    private val parser = TermParser.withDefaultOperators()

    private fun buildSolutionReply(id: String, solution: Solution): SolutionReply {
        return SolutionReply.newBuilder().setSolution(solution.substitution.toString()).setId(id).build()
    }

    private fun sendListOfReplies(id: String, solutions: Sequence<Solution>, responseObserver: StreamObserver<SolutionReply>) {
        solutions.forEach {
            responseObserver.onNext(
                buildSolutionReply(id, it)
            )
        }
        responseObserver.onCompleted()
    }

    private fun solveQuery(id: String, struct: String, options: SolveOptions = SolveOptions.DEFAULT): Sequence<Solution> {
        return solvers.getSolver(id).solve(parser.parseStruct(struct), options)
    }

    private fun checkId(id: String = ""): String {
        var actualId: String = id
        if(actualId == "") actualId = solvers.addSolver()
        return actualId
    }


    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        val id = checkId(request.id)
        sendListOfReplies(id, solveQuery(id, request.struct), responseObserver)
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout, responseObserver: StreamObserver<SolutionReply>) {
        val id = checkId(request.id)
        sendListOfReplies(
            id,
            solveQuery(id, request.struct, SolveOptions.allEagerlyWithTimeout(request.timeout)),
            responseObserver)
    }

    private fun parseOptions(options: List<Options>): SolveOptions {
        var lazyness = false
        var limit = SolveOptions.ALL_SOLUTIONS
        var timeout = SolveOptions.MAX_TIMEOUT

        options.forEach {
            when(it.name) {
                TIMEOUT_OPTION -> timeout = it.value
                LIMIT_OPTION -> limit = it.value.toInt()
                LAZY_OPTION -> lazyness = true
                EAGER_OPTION -> lazyness = false
            }
        }
        return SolveOptions.of(lazyness, timeout, limit)
    }

    override fun solveWithOptions(request: SolveRequestWithOptions, responseObserver: StreamObserver<SolutionReply>) {
        val id = checkId(request.id)
        sendListOfReplies(
            id,
            solveQuery(id, request.struct, parseOptions(request.optionsList)),
            responseObserver)
    }

    private fun putSolutionsInList(id: String, solutions: Sequence<Solution>): SolutionListReply {
        val solutionBuilder = SolutionListReply.newBuilder()
        solutions.mapIndexed { index, solution ->
            solutionBuilder.setSolution(index, buildSolutionReply(id, solution))
        }
        return solutionBuilder.build()
    }

    override fun solveList(request: SolveRequest, responseObserver: StreamObserver<SolutionListReply>) {
        val id = checkId(request.id)
        responseObserver.onNext(putSolutionsInList(id, solveQuery(id, request.struct)))
        responseObserver.onCompleted()
    }

    override fun solveListWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        val id = checkId(request.id)
        responseObserver.onNext(putSolutionsInList(
            id,
            solveQuery(id, request.struct, SolveOptions.allEagerlyWithTimeout(request.timeout))
        ))
        responseObserver.onCompleted()
    }

    override fun solveListWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        val id = checkId(request.id)
        responseObserver.onNext(putSolutionsInList(
            id,
            solveQuery(id, request.struct, parseOptions(request.optionsList))
        ))
        responseObserver.onCompleted()
    }

    override fun solveOnce(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        val id = checkId(request.id)
        sendListOfReplies(
            id,
            solveQuery(id, request.struct, SolveOptions.someEagerly(1)),
            responseObserver
        )
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        val id = checkId(request.id)
        sendListOfReplies(
            id,
            solveQuery(id, request.struct, SolveOptions.someEagerlyWithTimeout(1, request.timeout)),
            responseObserver
        )
    }

    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        val id = checkId(request.id)
        sendListOfReplies(
            id,
            solveQuery(id, request.struct, parseOptions(request.optionsList).setLimit(1)),
            responseObserver
        )
    }
}