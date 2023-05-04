package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve

class StreamedQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private val responseObserver: ConnectionClientObserver
    private val requestObserver: StreamObserver<SolverMsg>

    init {
        responseObserver = ConnectionClientObserver(request)
        requestObserver = GenericPrimitiveServiceGrpc.newStub(channel).callPrimitive(responseObserver)
        responseObserver.sendRequestOn(requestObserver)
    }

    /** Returns the head element received from the server.
     * @throws IllegalStateException if the stream is already over
     */
    fun popElement(): Solve.Response {
        if(responseObserver.isClosed) throw IllegalStateException()
        requestObserver.onNext(SolverMsg.newBuilder().setNext(EmptyMsg.getDefaultInstance()).build())
        return responseObserver.popElement()
    }

    val isClosed: Boolean
        get() = responseObserver.isClosed
}