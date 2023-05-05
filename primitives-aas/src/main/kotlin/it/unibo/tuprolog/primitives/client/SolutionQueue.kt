package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.client.session.SessionClientObserver
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

class SolutionQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private val responseObserver: SessionClientObserver

    init {
        responseObserver = SessionClientObserver(request)
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