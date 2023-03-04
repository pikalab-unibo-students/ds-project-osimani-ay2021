package it.unibo.tuprolog.solve.lpaas.server

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.lpaas.*
import it.unibo.tuprolog.solve.lpaas.server.utils.SolversCollection
import it.unibo.tuprolog.solve.lpaas.solverFactoryMessage.*

object SolverFactoryService: SolverFactoryGrpc.SolverFactoryImplBase() {

    private val solvers = SolversCollection

    override fun solverOf(request: SolverRequest, responseObserver: StreamObserver<SolverReply>) {
        val id = solvers.addSolver(request.staticKb.getClause(0).content,
            request.dynamicKb.getClause(0).content)
        responseObserver.onNext(SolverReply.newBuilder().setId(id).build())
        responseObserver.onCompleted()
    }
}