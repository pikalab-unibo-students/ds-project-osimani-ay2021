package it.unibo.tuprolog.solve.lpaas.server.channels

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.exception.Warning
import it.unibo.tuprolog.solve.lpaas.solveMessage.ReadLine

class ChannelsDequesCollector {
    private val inputs: MutableMap<String, InputChannelObserver<String>> = mutableMapOf()
    private val outputs: MutableMap<String, OutputChannelObserver<*>> = mutableMapOf()

    init {
        outputs[STDWARN] = OutputChannelObserver.of<Warning>()
    }

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

    fun addWarningChannel(name: String): OutputChannel<Warning> {
        val channel = OutputChannelObserver.of<Warning>()
        outputs[name] = channel
        return channel
    }

    fun getInputChannels(): Map<String, InputChannelObserver<String>> {
        return inputs
    }

    @Suppress("UNCHECKED_CAST")
    fun getOutputChannels(): Map<String, OutputChannelObserver<String>> {
        return outputs.mapNotNull {
            if(it.value.eventType == String::class)
                Pair(it.key, it.value as OutputChannelObserver<String>)
            else null}.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    fun getWarningChannel(): OutputChannel<Warning> {
        return outputs[STDWARN]!! as OutputChannel<Warning>
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

    fun readOnOutputChannel(name: String): String {
        if(outputs.containsKey(name))
            return outputs[name]!!.consumeFirst().toString()
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