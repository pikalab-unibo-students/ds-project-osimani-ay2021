package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.theory.parsing.ClausesParser

object SolverFactoryService: SolverFactoryGrpc.SolverFactoryImplBase() {

    private val solvers = SolversCollection


    override fun produceSolver(request: SolverRequest, responseObserver: StreamObserver<SolverReply>) {
        val id = solvers.addSolver(request.staticKb, request.dynamicKb)
        responseObserver.onNext(SolverReply.newBuilder().setId(id).build())
        responseObserver.onCompleted()
    }
}