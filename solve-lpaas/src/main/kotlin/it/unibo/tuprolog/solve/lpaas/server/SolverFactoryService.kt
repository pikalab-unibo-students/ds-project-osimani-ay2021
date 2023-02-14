package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesParser

object SolverFactoryService: SolverFactoryGrpc.SolverFactoryImplBase() {

    private val solvers = SolversCollection
    private val parser = ClausesParser.withDefaultOperators()

    override fun produceSolver(request: SolverRequest, responseObserver: StreamObserver<SolverReply>) {
        var staticKb: Theory
        var dynamicKb: Theory
        try {
            staticKb = parser.parseTheory(request.staticKb)
            dynamicKb = parser.parseTheory(request.dynamicKb)
        } catch (e: Exception) {
            staticKb = Theory.empty()
            dynamicKb = Theory.empty()
        }
        val id = solvers.addSolver(Solver.prolog.solverWithDefaultBuiltins(
            staticKb = staticKb,
            dynamicKb = dynamicKb
        ))
        responseObserver.onNext(SolverReply.newBuilder().setId(id).build())
        responseObserver.onCompleted()
    }
}