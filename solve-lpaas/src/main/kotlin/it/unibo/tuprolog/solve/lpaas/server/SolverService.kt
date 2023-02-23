package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.*

object SolverService : SolverGrpc.SolverImplBase() {


    private val solvers = SolversCollection
    private val parser = TermParser.withDefaultOperators()

    private fun buildSolutionReply(solution: Solution): SolutionReply {
        return SolutionReply.newBuilder()
            .setQuery(solution.query.toString())
            .setSolvedQuery(solution.solvedQuery.toString())
            .setSubstitution(solution.substitution.toString())
            .setIsYes(solution.isYes)
            .setIsNo(solution.isNo)
            .setIsHalt(solution.isHalt)
            .setError(solution.exception.toString()).build()
    }

    private fun sendListOfReplies(solutions: Sequence<Solution>, responseObserver: StreamObserver<SolutionReply>) {
        solutions.forEach {
            responseObserver.onNext(
                buildSolutionReply(it)
            )
        }
        responseObserver.onCompleted()
    }

    private fun solveQuery(id: String, struct: String, options: SolveOptions = SolveOptions.DEFAULT): Sequence<Solution> {
        return solvers.getSolver(id).solve(parser.parseStruct(struct), options)
    }

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(solveQuery(request.id, request.struct), responseObserver)
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(
            solveQuery(request.id, request.struct, SolveOptions.allLazilyWithTimeout(request.timeout)),
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
        sendListOfReplies(
            solveQuery(request.id, request.struct, parseOptions(request.optionsList)),
            responseObserver)
    }

    private fun solveQueryAsList(id: String, struct: String, options: SolveOptions = SolveOptions.DEFAULT):
        List<Solution> {
        return solvers.getSolver(id).solveList(parser.parseStruct(struct), options)
    }

    private fun putSolutionsInList(solutions: List<Solution>): SolutionListReply {
        val solutionBuilder = SolutionListReply.newBuilder()
        solutions.map {
            solutionBuilder.addSolution(buildSolutionReply(it))
        }
        return solutionBuilder.build()
    }

    override fun solveList(request: SolveRequest, responseObserver: StreamObserver<SolutionListReply>) {
        responseObserver.onNext(putSolutionsInList(solveQueryAsList(request.id, request.struct)))
        responseObserver.onCompleted()
    }

    override fun solveListWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        responseObserver.onNext(putSolutionsInList(
            solveQueryAsList(request.id, request.struct, SolveOptions.allEagerlyWithTimeout(request.timeout))
        ))
        responseObserver.onCompleted()
    }

    override fun solveListWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        responseObserver.onNext(putSolutionsInList(
            solveQueryAsList(request.id, request.struct, parseOptions(request.optionsList))
        ))
        responseObserver.onCompleted()
    }

    override fun solveOnce(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(
            solveQuery(request.id, request.struct, SolveOptions.someEagerly(1)),
            responseObserver
        )
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        sendListOfReplies(
            solveQuery(request.id, request.struct, SolveOptions.someEagerlyWithTimeout(1, request.timeout)),
            responseObserver
        )
    }

    /** NEED TO CHANGE BASIC IMPLEMENTATION, SOLUTION GIVEN WITH NEXT **/
    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        sendListOfReplies(
            solveQuery(request.id, request.struct, parseOptions(request.optionsList).setLimit(1)),
            responseObserver
        )
    }
}