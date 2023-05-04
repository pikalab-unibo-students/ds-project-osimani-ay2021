package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpc
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.primitive.Solve
import java.util.concurrent.LinkedBlockingDeque

class StreamedQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private var closed = false

    /** Returns true if the stream is over **/
    val isClosed: Boolean
        get() = closed

    private val msg = request.serialize()
    private val queue = LinkedBlockingDeque<Solve.Response>()

    private val requestObserver =
        GenericPrimitiveServiceGrpc.newStub(channel).callPrimitive(
            object: StreamObserver<ResponseMsg> {
                private val scope = Scope.of(request.query)

                override fun onNext(value: ResponseMsg) {
                    if(!value.hasNext) {
                        this.onCompleted()
                        closed = true;
                    }
                    queue.add(value.deserialize(scope))
                }

                override fun onError(t: Throwable?) {
                    println(t);
                    queue.add(
                        request.replyException(ResolutionException(
                            context = request.context,
                            cause = t))
                    )
                    closed = true;
                }

                override fun onCompleted() { closed = true }
        })

    /** Returns the head element received from the server.
     * @throws IllegalStateException if the stream is already over
     */
    fun popElement(): Solve.Response {
        if(isClosed) throw IllegalStateException()
        requestObserver.onNext(msg)
        return queue.takeFirst()
    }
}