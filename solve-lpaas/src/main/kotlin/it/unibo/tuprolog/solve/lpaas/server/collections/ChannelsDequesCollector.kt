package it.unibo.tuprolog.solve.lpaas.server.collections

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.lpaas.solveMessage.Channels
import it.unibo.tuprolog.solve.lpaas.solveMessage.LineEvent
import it.unibo.tuprolog.solve.lpaas.solveMessage.OutputChannelEvent
import it.unibo.tuprolog.solve.lpaas.util.toMap
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class ChannelsDequesCollector {
    private val inputs: MutableMap<String, Pair<BlockingDeque<String>, InputChannel<String>>> = mutableMapOf()
    private val outputs: MutableMap<String, Pair<BlockingDeque<String>, OutputChannel<String>>> = mutableMapOf()
    private val outputListeners: MutableMap<String, MutableList<StreamObserver<LineEvent>>> = mutableMapOf()

    fun addInputChannel(name: String, content: String = ""): InputChannel<String> {
        val deque = LinkedBlockingDeque<String>()
        val channel = InputChannel.of {
            inputs[name]?.first?.takeFirst()
        }
        inputs[name] = Pair(deque, channel)
        if(content.isNotEmpty()) writeOnInputChannel(name, content)
        return channel
    }

    fun addOutputChannel(name: String): OutputChannel<String> {
        val deque = LinkedBlockingDeque<String>()
        val channel = OutputChannel.of { line: String->
            if(outputListeners[name]?.isNotEmpty() == true) {
                outputListeners[name]!!.forEach { sendOutput(line, it) }
            } else {
                outputs[name]?.first?.putLast(line)
            }
        }
        outputs[name] = Pair(deque, channel)
        return channel
    }

    fun getInputChannels(): Map<String, InputChannel<String>> {
        return inputs.map { Pair(it.key, it.value.second) }.toMap()
    }

    fun getOutputChannels(): Map<String, OutputChannel<String>> {
        return outputs.map { Pair(it.key, it.value.second) }.toMap()
    }

    fun addListener(channelID: String, observer: StreamObserver<LineEvent>) {
        if(!outputListeners.containsKey(channelID))
            outputListeners[channelID] = mutableListOf()
        outputListeners[channelID]!!.add(observer)
        while(outputs[channelID]?.first?.isNotEmpty() == true) {
            sendOutput(outputs[channelID]!!.first.takeFirst().toString(), observer)
        }
    }

    fun removeListener(channelID: String, observer: StreamObserver<LineEvent>) {
        outputListeners[channelID]?.remove(observer)
        observer.onCompleted()
    }

    fun writeOnInputChannel(name: String, line: String = "") {
        line.toCharArray().forEach { char ->
            inputs[name]?.first!!.putLast(char.toString())
        }
    }

    fun readOnOutputChannel(name: String): String {
        if(outputs.containsKey(name))
           return outputs[name]!!.first.takeFirst()
        else throw IllegalArgumentException()
    }

    private fun sendOutput(line: String, observer: StreamObserver<LineEvent>) {
        observer.onNext(
            LineEvent.newBuilder().setLine(line).build()
        )
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