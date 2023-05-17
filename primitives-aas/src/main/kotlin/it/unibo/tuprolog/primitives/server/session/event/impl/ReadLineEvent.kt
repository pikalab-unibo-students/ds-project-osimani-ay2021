package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.SubResponseMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import it.unibo.tuprolog.primitives.server.session.event.SubRequestEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

class ReadLineEvent(
    override val id: String,
    channelName: String
): SubRequestEvent {

    override val message: GeneratorMsg = buildReadLineMsg(id, channelName)

    private val result: CompletableDeferred<LineMsg> = CompletableDeferred()

    override fun awaitResult(): String {
        val result = runBlocking {
            result.await()
        }
        if(result.hasContent()) {
            return result.content
        } else throw Exception("ReadLine operation failed")
    }

    override fun signalResponse(msg: SubResponseMsg) {
        if(msg.hasLine())
            this.result.complete(msg.line)
        else
            throw IllegalArgumentException()
    }
}