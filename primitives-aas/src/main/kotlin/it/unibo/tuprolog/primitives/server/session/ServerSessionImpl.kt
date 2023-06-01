package it.unibo.tuprolog.primitives.server.session

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.RequestMsg
import it.unibo.tuprolog.primitives.SolverMsg
import it.unibo.tuprolog.primitives.utils.idGenerator
import it.unibo.tuprolog.primitives.parsers.deserializers.distribuited.deserializeAsDistributed
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.serialize
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitive
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedResponse
import it.unibo.tuprolog.primitives.server.distribuited.DistributedRuntime
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.GetEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.SingleInspectKbEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.ReadLineEvent
import it.unibo.tuprolog.primitives.server.session.event.impl.SingleSubSolveEvent
import it.unibo.tuprolog.solve.data.CustomDataStore
import it.unibo.tuprolog.solve.flags.FlagStore
import it.unibo.tuprolog.unify.Unificator

/**
 * Represent the observer of a connection between the Primitive Server and a client,
 * generated from a call of the primitive
 */
class ServerSessionImpl(
    primitive: DistributedPrimitive,
    request: RequestMsg,
    private val responseObserver: StreamObserver<GeneratorMsg>,
): ServerSession {

    private val stream: Iterator<DistributedResponse>
    private val ongoingSubRequests: MutableList<SubRequestEvent> = mutableListOf()

    init {
        stream = primitive.solve(
            request.deserializeAsDistributed(this)
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
            println(msg)
            ongoingSubRequests.find { it.id == msg.response.id }.let {
                it?.signalResponse(msg.response)
            }
        }
        /** Throws error if it tries to initialize again */
        else if (msg.hasRequest()) {
            throw IllegalArgumentException("The request has already been initialized")
        }
    }

    override fun subSolve(query: Struct, timeout: Long): Sequence<DistributedResponse> =
        object: Iterator<DistributedResponse> {
            val id: String = idGenerator()
            private var hasNext: Boolean = true

            override fun hasNext(): Boolean =
                hasNext

            override fun next(): DistributedResponse {
                val request = SingleSubSolveEvent(id, query, timeout)

                return enqueueRequestAndAwait<DistributedResponse>(request)
                    .also {
                        hasNext = request.hasNext()!!
                    }
            }
        }.asSequence()

    override fun readLine(channelName: String): String {
        val request = ReadLineEvent(idGenerator(), channelName)
        return enqueueRequestAndAwait(request)
    }

    override fun inspectKB(
        kbType: Session.KbType,
        maxClauses: Long,
        vararg filters: Pair<Session.KbFilter, String>
    ): Sequence<Clause?> =
        object: Iterator<Clause?> {
            private val id = idGenerator()
            private var hasNext: Boolean = true

            override fun hasNext(): Boolean =
                hasNext

            override fun next(): Clause? {
                val request = SingleInspectKbEvent(id, kbType, maxClauses, *filters)

                return enqueueRequestAndAwait<Clause?>(request)
                    .also { hasNext = (it != null) }
            }
        }.asSequence()

    override fun getLogicStackTrace(): List<Struct> =
        enqueueRequestAndAwait(
            GetEvent.ofLogicStackTrace(idGenerator())
        )

    override fun getCustomDataStore(): CustomDataStore =
        enqueueRequestAndAwait(
            GetEvent.ofCustomDataStore(idGenerator())
        )

    override fun getUnificator(): Unificator =
        enqueueRequestAndAwait(
            GetEvent.ofUnificator(idGenerator())
        )


    override fun getLibraries(): DistributedRuntime =
        enqueueRequestAndAwait(
            GetEvent.ofLibraries(idGenerator())
        )

    override fun getFlagStore(): FlagStore =
        enqueueRequestAndAwait(
            GetEvent.ofFlagStore(idGenerator())
        )

    override fun getOperators(): OperatorSet =
        enqueueRequestAndAwait(
            GetEvent.ofOperators(idGenerator())
        )

    override fun getInputStoreAliases(): Set<String> =
        enqueueRequestAndAwait(
            GetEvent.ofInputChannels(idGenerator())
        )

    override fun getOutputStoreAliases(): Set<String> =
        enqueueRequestAndAwait(
            GetEvent.ofOutputChannels(idGenerator())
        )

    private inline fun <reified T> enqueueRequestAndAwait(request: SubRequestEvent): T {
        responseObserver.onNext(request.message)
        ongoingSubRequests.add(request)
        val result = request.awaitResult().also {
            ongoingSubRequests.remove(request)
        }
        return if(result is T)
            result
        else
            throw TypeCastException()
    }

}