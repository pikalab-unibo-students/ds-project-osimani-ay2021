package it.unibo.tuprolog.primitives.server.session.impl

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.*
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.primitives.server.session.ServerSession
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineHandler
import it.unibo.tuprolog.primitives.server.session.event.impl.SubSolveHandler
import it.unibo.tuprolog.solve.Solution
import java.util.concurrent.Executor

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ServerSessionImpl(
    private val primitive: DistribuitedPrimitive,
    private val responseObserver: StreamObserver<GeneratorMsg>,
    private val executor: Executor
): ServerSession, StreamObserver<SolverMsg> {

    private var stream: Iterator<DistributedResponse>? = null

    override fun onNext(
        msg: SolverMsg
    ) = when (stream) {
            null -> {
                if (msg.hasRequest()) {
                    stream = primitive.solve(msg.request.deserialize(this)).iterator()
                } else {
                    throw IllegalStateException("The request has not been received yet")
                }
            }
        else -> {
            executor.execute {
                handleEvent(msg)
            }
        }
    }

    override fun onError(t: Throwable?) {
        if (t!! is StatusRuntimeException &&
            (t as StatusRuntimeException).status.code == Status.CANCELLED.code)
            println("Connection ended by client")
        else {
            t.let {
                throw t
            }
        }
    }

    override fun onCompleted() {
        responseObserver.onCompleted()
    }

    private val subSolveHandler: SubSolveHandler = SubSolveHandler(responseObserver)
    private val readLineHandler: ReadLineHandler = ReadLineHandler(responseObserver)

    private fun handleEvent(event: SolverMsg) {
        /** Handling Next Request */
        if(event.hasNext()) {
            if(stream != null) {
                val solution = stream!!.next().serialize(stream!!.hasNext())
                responseObserver.onNext(
                    GeneratorMsg.newBuilder().setResponse(solution).build()
                )
                if (!stream!!.hasNext()) this.onCompleted()
            }
        }
        /** Handling SubSolve Solution Event */
        else if(event.hasSolution()) {
            subSolveHandler.handleResponse(event.solution)
        }
        /** Handling ReadLine Response Event */
        else if(event.hasLine()) {
            readLineHandler.handleResponse(event.line)
        }
        /** Throws error if it tries to initialize again */
        else if(event.hasRequest()) {
            throw IllegalArgumentException("The request has already been initialized")
        }
    }

    override fun subSolve(query: Struct): Sequence<Solution> = subSolveHandler.sendRequest(query)

    override fun readLine(channelName: String): String = readLineHandler.sendRequest(channelName)
}