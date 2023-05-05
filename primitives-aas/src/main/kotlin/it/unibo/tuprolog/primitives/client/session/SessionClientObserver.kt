package it.unibo.tuprolog.primitives.client.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.client.session.SessionSolver
import it.unibo.tuprolog.primitives.messages.EmptyMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.currentTimeInstant
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.LinkedBlockingDeque

class SessionClientObserver(private val request: Solve.Request<ExecutionContext>): StreamObserver<GeneratorMsg> {

    private var closed = false

    /** Returns true if all the solutions have been received **/
    val isClosed: Boolean
        get() = closed

    private val scope = Scope.of(request.query)
    private val sessionSolver: SessionSolver = SessionSolver.of(::getResponseObserver, request.context)
    private val queue = LinkedBlockingDeque<Solve.Response>()
    private var responseObserver: CompletableDeferred<StreamObserver<SolverMsg>> = CompletableDeferred()

    fun sendRequestOn(observer: StreamObserver<SolverMsg>) {
        if(!responseObserver.isCompleted) {
            responseObserver.complete(observer)
            observer.onNext(SolverMsg.newBuilder().setRequest(request.serialize()).build())
        } else println("REQUEST ALREADY SENT")
    }

    override fun onNext(value: GeneratorMsg) {
        handleEvent(value)
    }

    override fun onError(t: Throwable?) {
        println("from client $t")
        queue.add(
            request.replyException(ResolutionException(
                context = request.context,
                cause = t))
        )
        closed = true
    }

    override fun onCompleted() {
        closed = true
    }

    fun popElement(): Solve.Response {
        if(this.isClosed) throw IllegalStateException()
        getResponseObserver().onNext(
            SolverMsg.newBuilder().setNext(EmptyMsg.getDefaultInstance()).build()
        )
        return queue.takeFirst()
    }

    private fun handleEvent(event: GeneratorMsg) {
        if(event.hasResponse()) {
            queue.add(event.response.deserialize(scope))
            if(!event.response.solution.hasNext) {
                closed = true
                this.onCompleted()
            }
        }
        else if(event.hasReadLine()) {
            sessionSolver.readLine(event.readLine)
        }
        else if(event.hasSubSolve()) {
            sessionSolver.solve(event.subSolve)
        }
    }

    private fun getResponseObserver() =
        runBlocking {
            responseObserver.await()
        }
}