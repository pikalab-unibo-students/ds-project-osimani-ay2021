package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.ResponseMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.parsers.deserializers.distribuited.deserializeAsDistributed
import it.unibo.tuprolog.primitives.parsers.serializers.distribuited.buildSubSolveMsg
import it.unibo.tuprolog.primitives.server.distribuited.DistributedResponse
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class SingleSubSolveEvent(
    override val id: String,
    private val query: Struct,
    timeout: Long
): SubRequestEvent {

    override val message: GeneratorMsg = buildSubSolveMsg(query, id, timeout = timeout)

    private val result: CompletableDeferred<ResponseMsg> = CompletableDeferred()
    private var hasNext: Boolean? = null

    fun hasNext(): Boolean? = hasNext

    override fun awaitResult(): DistributedResponse {
        val response = runBlocking {
            result.await()
        }
        hasNext = response.solution.hasNext
        return response.deserializeAsDistributed()
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if(msg.hasSolution())
            this.result.complete(msg.solution)
        else
            throw IllegalArgumentException()
    }
}