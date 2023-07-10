package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.client.impl.ClientSessionImpl
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

interface ClientSession: StreamObserver<GeneratorMsg> {

    val solutionsQueue: Iterator<Solve.Response>

    companion object {
        fun of(
            request: Solve.Request<ExecutionContext>,
            channelBuilder: ManagedChannelBuilder<*>
        ): ClientSession =
            ClientSessionImpl(request, channelBuilder)

    }
}