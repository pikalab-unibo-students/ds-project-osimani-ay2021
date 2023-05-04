package it.unibo.tuprolog.primitives.client

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.primitive.Solve
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.LinkedBlockingDeque

class ConnectionClientObserver(private val request: Solve.Request<ExecutionContext>): StreamObserver<GeneratorMsg> {

    private var closed = false

    /** Returns true if all the solutions have been received **/
    val isClosed: Boolean
        get() = closed

    private val scope = Scope.of(request.query)
    private val solver = request.context.createSolver()
    private val computations: MutableMap<Struct, Iterator<Solution>> = mutableMapOf()
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
        println(t);
        queue.add(
            request.replyException(ResolutionException(
                context = request.context,
                cause = t))
        )
        closed = true;
    }

    override fun onCompleted() { closed = true }

    fun popElement(): Solve.Response = queue.takeFirst()

    private fun handleEvent(event: GeneratorMsg) {
        if(event.hasResponse()) {
            queue.add(event.response.deserialize(scope))
            if(!event.response.solution.hasNext) {
                closed = true
                this.onCompleted()
            }
        }
        else if(event.hasReadLine()) {
            solver.inputChannels[event.readLine.channelName]?.let {
                val line = it.read()!!
                getResponseObserver().onNext(
                    SolverMsg.newBuilder().setLine(LineMsg.newBuilder().setContent(line)).build()
                )
            }
        }
        else if(event.hasSubSolve()) {
            val subSolve = event.subSolve
            val query = subSolve.query.deserialize(scope)
            val solution: Solution =
                computations.putIfAbsent(query, solver.solve(query).iterator())!!.next()
            getResponseObserver().onNext(
                SolverMsg.newBuilder().setSolution(
                    solution.serialize(computations[query]!!.hasNext())
                ).build()
            )
        }
    }

    private fun getResponseObserver() =
        runBlocking {
            responseObserver.await()
        }
}