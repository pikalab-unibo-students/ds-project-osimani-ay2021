package it.unibo.tuprolog.solve.lpaas.server.channels

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.lpaas.solveMessage.ReadLine

class ChannelsDequesCollector {
    private val inputs: MutableMap<String, InputChannelObserver<String>> = mutableMapOf()
    private val outputs: MutableMap<String, OutputChannelObserver<String>> = mutableMapOf()

    fun addInputChannel(name: String, content: List<String> = emptyList()): InputChannel<String> {
        val channel = InputChannelObserver.of(content)
        inputs[name] = channel
        return channel
    }

    fun addOutputChannel(name: String, content: List<String> = emptyList()): OutputChannel<String> {
        val channel = OutputChannelObserver.of<String>(content)
        outputs[name] = channel
        return channel
    }

    fun getInputChannels(): Map<String, InputChannelObserver<String>> {
        return inputs
    }

    fun getOutputChannels(): Map<String, OutputChannelObserver<String>> {
        return outputs.mapNotNull {
                Pair(it.key, it.value)
            }.toMap()
    }

    fun addListener(channelID: String, observer: StreamObserver<ReadLine>) {
        outputs[channelID]?.addObserverListener(observer)
    }

    fun removeListener(channelID: String, observer: StreamObserver<ReadLine>) {
        outputs[channelID]?.removeObserverListener(observer)
    }

    fun writeOnInputChannel(name: String, line: String = "") {
        inputs[name]?.writeOnChannel(line)
    }

    fun consumeFromOutputChannel(name: String): String {
        if(outputs.containsKey(name))
            return outputs[name]!!.consumeFirst()
        else throw IllegalArgumentException()
    }

    companion object {
        const val STDWARN = "warning"
        fun of(inputs: Map<String, List<String>> = emptyMap(),
               outputs: Map<String, List<String>> = emptyMap()): ChannelsDequesCollector {
            val collection = ChannelsDequesCollector()
            inputs.forEach { collection.addInputChannel(it.key, it.value) }
            outputs.forEach { collection.addOutputChannel(it.key, it.value) }
            return collection
        }
    }
}