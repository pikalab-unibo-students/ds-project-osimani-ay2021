package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive

interface ServerSession: Session {

    fun handleMessage(msg: SolverMsg)

    companion object {

        fun of(
            primitive: DistribuitedPrimitive,
            request: RequestMsg,
            responseObserver: StreamObserver<GeneratorMsg>
        ): ServerSession =
            ServerSessionImpl(primitive, request, responseObserver)
    }
}
