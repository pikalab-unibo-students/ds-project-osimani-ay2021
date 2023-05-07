package it.unibo.tuprolog.primitives.server.session.event.impl

import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import it.unibo.tuprolog.primitives.server.session.event.ServerEvent
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.reflect.KSuspendFunction1

class ReadLineHandler(private val emit: KSuspendFunction1<GeneratorMsg, Unit>):
    ServerEvent<String, LineMsg, String> {

    private val readLineMap: MutableMap<String, Channel<LineMsg>> = mutableMapOf()

    override suspend fun applyEvent(input: String): String {
        readLineMap.putIfAbsent(input, Channel())
        emit(buildReadLineMsg(input))
        val result = readLineMap[input]!!.receive()
        if(result.hasError()) {
            throw Exception(
                when(result.error) {
                    LineMsg.Error.EMPTY_CHANNEL -> "Channel was empty"
                    LineMsg.Error.CHANNEL_NOT_FOUND -> "Channel Not Fount"
                    else -> result.error.toString()
                })
        } else return result.content
    }

    override suspend fun handleResponse(response: LineMsg) {
        readLineMap[response.channelName]?.send(response)
    }
}