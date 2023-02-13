package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.theory.Theory

object SolverService : SolverGrpc.SolverImplBase() {

    private val theory = Theory.of(
        Fact.of( Struct.of("f", Atom.of("a"))), // f(a).
        Fact.of( Struct.of("f", Atom.of("b"))), // f(b).
        Fact.of( Struct.of("f", Atom.of("c")))
    )

    val solver = Solver.prolog.solverWithDefaultBuiltins(staticKb = theory)
    val parser = TermParser.withDefaultOperators()

    private fun buildSolutionReply(solution: Solution): SolutionReply {
        return SolutionReply.newBuilder().setSolution(solution.substitution.toString()).build()
    }

    private fun sendListOfReplies(solutions: Sequence<Solution>, responseObserver: StreamObserver<SolutionReply>) {
        solutions.forEach {
            responseObserver.onNext(
                buildSolutionReply(it)
            )
        }
        responseObserver.onCompleted()
    }

    private fun solveQuery(struct: String, options: SolveOptions = SolveOptions.DEFAULT): Sequence<Solution> {
        return solver.solve(parser.parseStruct(struct), options)
    }

    /*private fun launchWithTimer(f: () -> Unit, timeout: Long) {
        runBlocking { withTimeout(timeout*1000) {
            f()
        } }
    }*/

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(solveQuery(request.struct), responseObserver)
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(
            solveQuery(request.struct, SolveOptions.allEagerlyWithTimeout(request.timeout)),
            responseObserver)
    }

    private fun parseOptions(options: List<Options>): SolveOptions {
        var lazyness = false
        var limit = SolveOptions.ALL_SOLUTIONS
        var timeout = SolveOptions.MAX_TIMEOUT

        options.forEach {
            when(it.name) {
                "timeout" -> timeout = it.timeout
                "limit" -> limit = it.limit
                "lazyness" -> lazyness = true
                "eagerness" -> lazyness = false
            }
        }
        return SolveOptions.of(lazyness, timeout, limit)
    }

    override fun solveWithOption(request: SolveRequestWithOptions, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(
            solveQuery(request.struct, parseOptions(request.optionsList)),
            responseObserver)
    }

    private fun putSolutionsInList(solutions: Sequence<Solution>): SolutionListReply {
        val solutionBuilder = SolutionListReply.newBuilder()
        solutions.mapIndexed { index, solution ->
            solutionBuilder.setSolution(index, buildSolutionReply(solution))
        }
        return solutionBuilder.build()
    }

    override fun solveList(request: SolveRequest, responseObserver: StreamObserver<SolutionListReply>) {
        responseObserver.onNext(putSolutionsInList(solveQuery(request.struct)))
        responseObserver.onCompleted()
    }

    override fun solveListWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        responseObserver.onNext(putSolutionsInList(
            solveQuery(request.struct, SolveOptions.allEagerlyWithTimeout(request.timeout))
        ))
        responseObserver.onCompleted()
    }

    override fun solveListWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionListReply>
    ) {
        responseObserver.onNext(putSolutionsInList(
            solveQuery(request.struct, parseOptions(request.optionsList))
        ))
        responseObserver.onCompleted()
    }

    override fun solveOnce(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        sendListOfReplies(
            solveQuery(request.struct, SolveOptions.someEagerly(1)),
            responseObserver
        )
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        sendListOfReplies(
            solveQuery(request.struct, SolveOptions.someEagerlyWithTimeout(1, request.timeout)),
            responseObserver
        )
    }

    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<SolutionReply>
    ) {
        sendListOfReplies(
            solveQuery(request.struct, parseOptions(request.optionsList).setLimit(1)),
            responseObserver
        )
    }
}