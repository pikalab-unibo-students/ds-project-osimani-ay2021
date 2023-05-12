package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.solve.Solution

interface ServerSession {

    fun handleMessage(msg: SolverMsg)

    /** Request the client solver to resolve a query. It can be blocking.
     * @return the solutions computed
     */
    fun subSolve(query: Struct, timeout: Long): Sequence<Solution>

    /** Reads a character from an Input channel of the Solver.
     * @return the line read
     * @throws Exception if the read fails
     */
    fun readLine(channelName: String): String

    companion object {

        fun of(
            primitive: DistribuitedPrimitive,
            request: RequestMsg,
            responseObserver: StreamObserver<GeneratorMsg>
        ): ServerSession =
            ServerSessionImpl(primitive, request, responseObserver)
    }
}
