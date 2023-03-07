package it.unibo.tuprolog.solve.lpaas.server.utils

import it.unibo.tuprolog.solve.lpaas.util.toMap
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class ChannelsDequesCollector(inputs: Set<String> = emptySet(),
                              outputs: Set<String> = emptySet(),) {
    private val inputs: MutableMap<String, BlockingDeque<String>> = mutableMapOf()
    private val outputs: MutableMap<String, BlockingDeque<String>> = mutableMapOf()

    init {
        inputs.forEach { this.inputs[it] = LinkedBlockingDeque() }
        outputs.forEach { this.outputs[it] = LinkedBlockingDeque() }
    }

    fun getAllInputs(): Map<String, BlockingDeque<String>> {
        return inputs
    }

    fun getAllOutputs(): Map<String, BlockingDeque<String>> {
        return outputs.map { Pair(it.key, it.value) }.toMap()
    }

    fun writeOnInputChannel(name: String, line: String = "") {
        line.toCharArray().forEach { inputs[name]?.putLast(it.toString()) }
    }

    fun readOnOutputChannel(name: String): String {
        if(outputs.containsKey(name))
           return outputs[name]!!.takeFirst()
        else throw IllegalArgumentException()
    }
}