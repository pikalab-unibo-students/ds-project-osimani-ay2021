package it.unibo.tuprolog.solve.lpaas.server.collections

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.libs.oop.identifier
import it.unibo.tuprolog.solve.lpaas.solveMessage.LineEvent

class ChannelsDequesCollector {
    private val inputs: MutableMap<String, InputChannelObserver<String>> = mutableMapOf()
    private val outputs: MutableMap<String, OutputChannelObserver<String>> = mutableMapOf()

    fun addInputChannel(name: String, content: String = ""): InputChannel<String> {
        val channel = InputChannelObserver.of(content.toCharArray().map { it.toString() })
        inputs[name] = channel
        return channel
    }

    fun addOutputChannel(name: String): OutputChannel<String> {
        val channel = OutputChannelObserver.of<String>()
        outputs[name] = channel
        return channel
    }

    fun getInputChannels(): Map<String, InputChannel<String>> {
        return inputs
    }

    fun getOutputChannels(): Map<String, OutputChannel<String>> {
        return outputs
    }

    fun addListener(channelID: String, observer: StreamObserver<LineEvent>) {
        outputs[channelID]?.addObserverListener(observer)
    }

    fun removeListener(channelID: String, observer: StreamObserver<LineEvent>) {
        outputs[channelID]?.removeObserverListener(observer)
    }

    fun writeOnInputChannel(name: String, line: String = "") {
        inputs[name]?.writeOnChannel(line.toCharArray().map { it.toString() })
    }

    fun readOnOutputChannel(name: String): String {
        if(outputs.containsKey(name))
           return outputs[name]!!.queue.takeFirst().item!!
        else throw IllegalArgumentException()
    }

    companion object {
        fun of(inputs: Map<String, String> = emptyMap(),
               outputs: Set<String> = emptySet()): ChannelsDequesCollector {
            val collection = ChannelsDequesCollector()
            inputs.forEach { collection.addInputChannel(it.key, it.value) }
            outputs.forEach { collection.addOutputChannel(it) }
            return collection
        }
    }
}