package it.unibo.tuprolog.primitives.server.session.event

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.primitives.GeneratorMsg
import it.unibo.tuprolog.primitives.LineMsg
import it.unibo.tuprolog.primitives.parsers.serializers.buildReadLineMsg
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ReadLineHandler(private val responseObserver: StreamObserver<GeneratorMsg>) {

    private val readLineMap: MutableMap<String, BlockingQueue<LineMsg>> = mutableMapOf()
    fun sendRequest(input: String): String {
        readLineMap.putIfAbsent(input, LinkedBlockingQueue())
        responseObserver.onNext(
            buildReadLineMsg(input)
        )
        val result = readLineMap[input]!!.take()
        if(result.hasContent()) {
            return result.content
        } else throw Exception("ReadLine operation failed")
    }

    fun handleResponse(response: LineMsg) {
        readLineMap[response.channelName]?.add(response)
    }
}