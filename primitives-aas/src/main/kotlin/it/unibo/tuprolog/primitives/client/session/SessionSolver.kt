package it.unibo.tuprolog.primitives.client.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.solve.ExecutionContext

interface SessionSolver {

    fun solve(event: SubSolveRequest)

    fun readLine(event: ReadLineMsg)

    companion object {
        fun of(responseObserver: () -> StreamObserver<SolverMsg>,
               executionContext: ExecutionContext
        ): SessionSolverImpl = SessionSolverImpl(responseObserver, executionContext.createSolver())
    }
}