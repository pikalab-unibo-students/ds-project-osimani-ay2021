package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.event.ReadLineHandler
import it.unibo.tuprolog.primitives.server.session.event.SubSolveHandler
import it.unibo.tuprolog.solve.Solution

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ServerSessionImpl(
    primitive: DistribuitedPrimitive,
    request: RequestMsg,
    private val responseObserver: StreamObserver<GeneratorMsg>,
): ServerSession {

    private val stream: Iterator<DistributedResponse>
    private val subSolveHandler: SubSolveHandler = SubSolveHandler(responseObserver)
    private val readLineHandler: ReadLineHandler = ReadLineHandler(responseObserver)

    init {
        stream = primitive.solve(
            request.deserialize(this)
        ).iterator()
    }

    override fun handleMessage(msg: SolverMsg) {
        /** Handling Next Request */
        if (msg.hasNext()) {
            val solution = stream.next().serialize(stream.hasNext())
            responseObserver.onNext(
                GeneratorMsg.newBuilder().setResponse(solution).build()
            )
            if (!stream.hasNext()) responseObserver.onCompleted()
        }
        /** Handling SubSolve Solution Event */
        else if (msg.hasSolution()) {
            subSolveHandler.handleResponse(msg.solution)
        }
        /** Handling ReadLine Response Event */
        else if (msg.hasLine()) {
            readLineHandler.handleResponse(msg.line)
        }
        /** Throws error if it tries to initialize again */
        else if (msg.hasRequest()) {
            throw IllegalArgumentException("The request has already been initialized")
        }
    }

    override fun subSolve(query: Struct, timeout: Long): Sequence<Solution> =
        subSolveHandler.sendRequest(query, timeout)

    override fun readLine(channelName: String): String =
        readLineHandler.sendRequest(channelName)
}