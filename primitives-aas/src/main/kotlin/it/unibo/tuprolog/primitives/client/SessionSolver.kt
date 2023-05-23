package it.unibo.tuprolog.primitives.client

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.InspectKbMsg
import it.unibo.tuprolog.primitives.ReadLineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.SubSolveRequest
import it.unibo.tuprolog.primitives.client.impl.SessionSolverImpl
import it.unibo.tuprolog.primitives.server.session.event.impl.GetEvent
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator

interface SessionSolver {

    /** Solve a query requested by the primitive server and sends the result.
     *  It can be blocking */
    fun solve(id: String, event: SubSolveRequest)

    /** Reads a character from an Input channel and sends it to the Primitive Server.
     *  It returns 'failed' if the read fails.
     */
    fun readLine(id: String, event: ReadLineMsg)

    /** Inspect a Kb with eventual filters and returns a filtered Theory
     */
    fun inspectKb(id: String, event: InspectKbMsg)

    fun getLogicStackTrace(id: String)

    fun getCustomDataStore(id: String)

    fun getUnificator(id: String)

    fun getLibraries(id: String)

    fun getFlagStore(id: String)

    fun getOperators(id: String)

    fun getInputStoreAliases(id: String)

    fun getOutputStoreAliases(id: String)

    companion object {
        fun of(
            responseObserver: StreamObserver<SolverMsg>,
            executionContext: ExecutionContext
        ): SessionSolverImpl =
            SessionSolverImpl(responseObserver, executionContext)
    }
}