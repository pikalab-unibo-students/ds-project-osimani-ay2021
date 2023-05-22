package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitive

interface ServerSession: Session, ContextRequester {

    fun handleMessage(msg: SolverMsg)

    companion object {

        fun of(
            primitive: DistributedPrimitive,
            request: RequestMsg,
            responseObserver: StreamObserver<GeneratorMsg>
        ): ServerSession =
            ServerSessionImpl(primitive, request, responseObserver)
    }
}
