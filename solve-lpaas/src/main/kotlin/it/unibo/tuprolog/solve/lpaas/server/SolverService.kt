package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.core.parsing.parse
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.util.*

object SolverService : SolverGrpc.SolverImplBase() {


    private val solvers = SolversCollection
    private val parser = TermParser.withDefaultOperators()

    private fun solveQuery(id: String, struct: String, responseObserver: StreamObserver<IteratorReply>,
                           options: SolveOptions = SolveOptions.DEFAULT) {
        ComputationsCollection.addIterator(id, struct, options)
        responseObserver.onNext(
            IteratorReply.newBuilder().setId(id).setQuery(struct).build()
        )
        responseObserver.onCompleted()
    }

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<IteratorReply>) {
       solveQuery(request.id, request.struct, responseObserver)
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout, responseObserver: StreamObserver<IteratorReply>) {
            solveQuery(request.id, request.struct,
                responseObserver, SolveOptions.allLazilyWithTimeout(request.timeout))
    }

    private fun parseOptions(options: List<Options>): SolveOptions {
        var laziness = false
        var limit = SolveOptions.ALL_SOLUTIONS
        var timeout = SolveOptions.MAX_TIMEOUT

        options.forEach {
            when(it.name) {
                TIMEOUT_OPTION -> timeout = it.value
                LIMIT_OPTION -> limit = it.value.toInt()
                LAZY_OPTION -> laziness = true
                EAGER_OPTION -> laziness = false
            }
        }
        return SolveOptions.of(laziness, timeout, limit)
    }

    override fun solveWithOptions(request: SolveRequestWithOptions, responseObserver: StreamObserver<IteratorReply>) {
            solveQuery(request.id, request.struct, responseObserver, parseOptions(request.optionsList))
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

    override fun solveOnce(request: SolveRequest, responseObserver: StreamObserver<IteratorReply>) {
        solveQuery(request.id, request.struct, responseObserver, SolveOptions.someEagerly(1))
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout,
        responseObserver: StreamObserver<IteratorReply>
    ) {
        solveQuery(request.id, request.struct, responseObserver,
            SolveOptions.someEagerlyWithTimeout(1, request.timeout))
    }

    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions,
        responseObserver: StreamObserver<IteratorReply>
    ) {
        solveQuery(request.id, request.struct, responseObserver,
            parseOptions(request.optionsList).setLimit(1))
    }

    override fun nextSolution(request: NextSolutionRequest, responseObserver: StreamObserver<SolutionReply>) {
        responseObserver.onNext(
            buildSolutionReply(ComputationsCollection.getNextSolution(request.id, request.query))
        )
        responseObserver.onCompleted()
    }

    private fun buildSolutionReply(solution: Solution): SolutionReply {
        val solutionBuilder = SolutionReply.newBuilder()
            .setQuery(solution.query.toString())
            .setIsYes(solution.isYes)
            .setIsNo(solution.isNo)
            .setIsHalt(solution.isHalt)
        if(solution.substitution.isSuccess) {
            solution.substitution.asIterable().forEach {
                solutionBuilder.addSubstitution(Substitution.newBuilder()
                    .setVar(it.key.name)
                    .setTerm(it.value.toString()))
            }
        }
        if(solution.exception != null)
            solution.exception
        return solutionBuilder.build()
    }
}