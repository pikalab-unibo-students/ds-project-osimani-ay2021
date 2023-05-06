package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import it.unibo.tuprolog.primitives.server.session.event.ServerEvent
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KSuspendFunction1

class ReadLineHandler(private val emit: KSuspendFunction1<GeneratorMsg, Unit>):
    ServerEvent<String, LineMsg, String> {

    private val readLineMap: MutableMap<String, BlockingQueue<LineMsg>> = mutableMapOf()

    override suspend fun applyEvent(input: String): String {
        readLineMap.putIfAbsent(input, LinkedBlockingQueue())
        emit(buildReadLineMsg(input))
        return readLineMap[input]!!.take().content
    }

    override fun handleResponse(response: LineMsg) {
        readLineMap[response.channelName]?.add(response)
    }
}