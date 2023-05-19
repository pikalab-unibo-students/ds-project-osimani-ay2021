package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.idGenerator
import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.InspectKbEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.SingleSubSolveEvent
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.theory.Theory

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
    private val ongoingSubRequests: MutableList<SubRequestEvent> = mutableListOf()

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
        /** Handling SubRequest Event */
        else if (msg.hasResponse()) {
            ongoingSubRequests.find { it.id == msg.response.id }.let {
                it?.signalResponse(msg.response)
            }
        }
        /** Throws error if it tries to initialize again */
        else if (msg.hasRequest()) {
            throw IllegalArgumentException("The request has already been initialized")
        }
    }

    override fun subSolve(query: Struct, timeout: Long): Sequence<Solution> =
        object: Iterator<Solution> {
            val id: String = idGenerator()
            private var hasNext: Boolean = true

            override fun hasNext(): Boolean =
                hasNext

            override fun next(): Solution {
                val request = SingleSubSolveEvent(id, query, timeout)
                enqueueGeneratorRequest(request)
                return request.awaitResult().also {
                    hasNext = request.hasNext()!!
                    ongoingSubRequests.remove(request)
                }
            }
        }.asSequence()

    override fun readLine(channelName: String): String {
        val request = ReadLineEvent(idGenerator(), channelName)
        enqueueGeneratorRequest(request)
        return request.awaitResult().also {
            ongoingSubRequests.remove(request)
        }
    }

    override fun inspectKB(
        kbType: Session.KbType,
        maxClauses: Long,
        vararg filters: Pair<Session.KbFilter, String>
    ): Theory {
        val request = InspectKbEvent(idGenerator(), kbType, maxClauses, *filters)
        enqueueGeneratorRequest(request)
        return request.awaitResult().also {
            ongoingSubRequests.remove(request)
        }
    }

    private fun enqueueGeneratorRequest(request: SubRequestEvent) {
        responseObserver.onNext(request.message)
        ongoingSubRequests.add(request)
    }

}