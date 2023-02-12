package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.theory.Theory
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.runBlocking

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

    private fun launchWithTimer(f: () -> Unit, timeout: Long) {
        runBlocking {  withTimeout(timeout*1000) {
            delay(3000)
            f()
        } }
    }

    override fun solve(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        val struct = parser.parseStruct(request.struct)
        solver.solve(struct).forEach {
            responseObserver.onNext(
                buildSolutionReply(it)
            )
        }
        responseObserver.onCompleted()
    }

    override fun solveWithTimeout(request: SolveRequestWithTimeout, responseObserver: StreamObserver<SolutionReply>) {
        val struct = parser.parseStruct(request.struct)
        launchWithTimer({
            solver.solve(struct).forEach {
                responseObserver.onNext(
                    buildSolutionReply(it)
                )
            }}
            ,
            request.timeout
        )
        responseObserver.onCompleted()
    }

    override fun solveWithOption(request: SolveRequestWithOptions, responseObserver: StreamObserver<SolutionReply>) {
        //val struct = parser.parseStruct(request.struct)
        //TO-DO
        responseObserver.onCompleted()
    }

    override fun solveList(request: SolveRequest, responseObserver: StreamObserver<SolutionListReply>) {
        val struct = parser.parseStruct(request.struct)
        val solutionBuilder = SolutionListReply.newBuilder()
        solver.solveList(struct).mapIndexed { index, solution ->
            solutionBuilder.setSolution(index, buildSolutionReply(solution))
        }
        responseObserver.onNext(solutionBuilder.build())
        responseObserver.onCompleted()
    }

    override fun solveListWithTimeout(
        request: SolveRequestWithTimeout?,
        responseObserver: StreamObserver<SolutionListReply>?
    ) {
        //super.solveListWithTimeout(request, responseObserver)
    }

    override fun solveListWithOptions(
        request: SolveRequestWithOptions?,
        responseObserver: StreamObserver<SolutionListReply>?
    ) {
        //super.solveListWithOptions(request, responseObserver)
    }

    override fun solveOnce(request: SolveRequest, responseObserver: StreamObserver<SolutionReply>) {
        val struct = parser.parseStruct(request.struct)
        responseObserver.onNext(
            buildSolutionReply(solver.solveOnce(struct))
        )
        responseObserver.onCompleted()
    }

    override fun solveOnceWithTimeout(
        request: SolveRequestWithTimeout?,
        responseObserver: StreamObserver<SolutionReply>?
    ) {
        //super.solveOnceWithTimeout(request, responseObserver)
    }

    override fun solveOnceWithOptions(
        request: SolveRequestWithOptions?,
        responseObserver: StreamObserver<SolutionReply>?
    ) {
        //super.solveOnceWithOptions(request, responseObserver)
    }
}