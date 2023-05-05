package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

class StreamedQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private val responseObserver: ConnectionClientObserver

    init {
        responseObserver = ConnectionClientObserver(request)
        val requestObserver = GenericPrimitiveServiceGrpc.newStub(channel).callPrimitive(responseObserver)
        responseObserver.sendRequestOn(requestObserver)
    }

    /** Returns the head element received from the server.
     * @throws IllegalStateException if the stream is already over
     */
    fun popElement(): Solve.Response {
        return responseObserver.popElement()
    }

    val isClosed: Boolean
        get() = responseObserver.isClosed
}