package it.unibo.tuprolog.primitives.client

import io.grpc.ManagedChannel
import it.unibo.tuprolog.primitives.GenericPrimitiveServiceGrpcKt.GenericPrimitiveServiceCoroutineStub
import it.unibo.tuprolog.primitives.client.impl.FlowDispatcherImpl
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

class SolutionQueue(request: Solve.Request<ExecutionContext>, channel: ManagedChannel) {

    private val dispatcher = FlowDispatcherImpl(request)

    init {
        val input = GenericPrimitiveServiceCoroutineStub(channel).callPrimitive(
            flow {
                emit(dispatcher.getMessage())
            }
        )
        runBlocking {
            while(!dispatcher.isClosed) {
                input.collect {
                    dispatcher.handleMessage(it)
                }
            }
        }
    }

    /** Returns the head element received from the server.
     * @throws IllegalStateException if the stream is already over
     */
    fun popElement(): Solve.Response {
        if(!isOver) {
            return runBlocking {
                dispatcher.popResponse()
            }
        } else throw IllegalStateException()
    }

    val isOver: Boolean
        get() = dispatcher.isClosed
}