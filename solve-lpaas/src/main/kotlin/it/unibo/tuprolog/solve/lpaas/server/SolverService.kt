package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.SolveOptions
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.server.utils.ComputationsCollection
import it.unibo.tuprolog.solve.lpaas.util.EAGER_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LAZY_OPTION
import it.unibo.tuprolog.solve.lpaas.util.LIMIT_OPTION
import it.unibo.tuprolog.solve.lpaas.util.TIMEOUT_OPTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object SolverService : SolverGrpc.SolverImplBase() {

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionSequence>) {
        val computationID = ComputationsCollection.addIterator(request.solverID, request.struct,
            parseOptions(request.optionsList))
        responseObserver.onNext(
            SolutionSequence.newBuilder().setSolverID(request.solverID)
                .setComputationID(computationID).setQuery(request.struct).build()
        )
        responseObserver.onCompleted()
    }

    override fun getSolution(request: SolutionID, responseObserver: StreamObserver<SolutionReply>) {
        val message: SolutionReply = try {
            val solution = runBlocking {
                ComputationsCollection.getSolution(request.solverID, request.computationID,
                    request.query, request.index)
            }
            buildSolutionReply(solution)
        } catch (e: Error) {
            SolutionReply.newBuilder().setQuery(request.query).setIsNo(true).setError(e.toString()).build()
        }
        responseObserver.onNext( message )
        responseObserver.onCompleted()
    }

    private fun parseOptions(options: List<Options>): SolveOptions {
        var laziness = true
        var limit = SolveOptions.ALL_SOLUTIONS
        var timeout = SolveOptions.MAX_TIMEOUT
        SolveOptions.DEFAULT
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
            solutionBuilder.error = solution.exception.toString()
        return solutionBuilder.build()
    }
}