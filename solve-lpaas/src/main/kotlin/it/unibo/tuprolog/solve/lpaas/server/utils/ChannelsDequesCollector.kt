package it.unibo.tuprolog.solve.lpaas.server.utils

import java.lang.IllegalArgumentException
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class ChannelsDequesCollector(inputs: Set<String> = emptySet(),
                              outputs: Set<String> = emptySet(),) {
    private val inputs: MutableMap<String, BlockingDeque<String>> = mutableMapOf()
    private val outputs: MutableMap<String, BlockingDeque<String>> = mutableMapOf()

    init {
        inputs.forEach { writeOnInputChannel(it) }
        outputs.forEach { generateOutputListener(it) }
    }

    fun getInputDeque(name: String): BlockingDeque<String>? {
        return inputs[name]
    }

    fun getOutputDeque(name: String): BlockingDeque<String>? {
        return outputs[name]
    }

    fun getAllInputs(): Map<String, BlockingDeque<String>> {
        return inputs
    }

    fun getAllOutputs(): Map<String, BlockingDeque<String>> {
        return outputs
    }

    fun writeOnInputChannel(name: String, line: String = ""): BlockingDeque<String> {
        val deque: BlockingDeque<String> = if(inputs.containsKey(name))
            inputs[name]!! else LinkedBlockingDeque()
        line.toCharArray().forEach { inputs[name]?.putLast(it.toString()) }
        return deque
    }

    fun readFromOutputChannel(name: String): String {
        if(outputs.containsKey(name))
            return outputs[name]!!.takeFirst()
        else throw IllegalArgumentException()
    }

    fun generateOutputListener(name: String): BlockingDeque<String> {
        val deque = LinkedBlockingDeque<String>()
        outputs[name] = deque
        return deque
    }
}