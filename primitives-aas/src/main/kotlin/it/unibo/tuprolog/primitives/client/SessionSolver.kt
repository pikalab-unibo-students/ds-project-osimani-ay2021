package it.unibo.tuprolog.primitives.client

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.impl.SessionSolverImpl
import it.unibo.tuprolog.solve.ExecutionContext

interface SessionSolver {

    /** Solve a query requested by the primitive server and sends the result.
     *  It can be blocking */
    fun solve(id: String, event: SubSolveRequest)

    /** Reads a character from an Input channel and sends it to the Primitive Server.
     *  It returns 'failed' if the read fails.
     */
    fun readLine(id: String, event: ReadLineMsg)

    companion object {
        fun of(
            responseObserver: StreamObserver<SolverMsg>,
            executionContext: ExecutionContext
        ): SessionSolverImpl =
            SessionSolverImpl(responseObserver, executionContext.createSolver())
    }
}