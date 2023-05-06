package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpcKt.GenericPrimitiveServiceCoroutineStub
import it.unibo.tuprolog.primitives.client.session.SessionClientObserver
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.flow.flow

class SolutionQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private val responseObserver: SessionClientObserver

    init {
        responseObserver = SessionClientObserver(request)
        val requestObserver = GenericPrimitiveServiceCoroutineStub(channel).callPrimitive(
            flow {

            }
        ).collect {
        }
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