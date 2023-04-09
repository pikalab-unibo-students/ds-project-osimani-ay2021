package it.unibo.tuprolog.solve.lpaas.server.channels

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.Listener
import it.unibo.tuprolog.solve.channel.impl.AbstractOutputChannel
import it.unibo.tuprolog.solve.lpaas.solveMessage.ReadLine
import it.unibo.tuprolog.solve.lpaas.util.parsers.MessageBuilder.fromReadLineToMsg
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import kotlin.reflect.KClass

class OutputChannelObserver<T : Any>(
    val eventType: KClass<T>,
    private val queue: BlockingDeque<T> = LinkedBlockingDeque()
) : ChannelObserver<T>, AbstractOutputChannel<T>() {

    private val observers: MutableMap<StreamObserver<ReadLine>, Listener<T?>> = mutableMapOf()

    override fun writeActually(value: T) {
        queue.putLast(value)
    }

    override fun flushActually() {
        // does nothing
    }

    override fun close() {
        try {
            this.observers.forEach { it.key.onCompleted() }
        } catch (_: Exception) {}
        try {
            super.close()
        } catch (_: Exception) {}
    }

    override fun getCurrentContent(): List<T> = queue.toList()

    fun consumeFirst(): T = queue.takeFirst()

    fun addObserverListener(observer: StreamObserver<ReadLine>) {
        val listener: Listener<T?> = {
            sendOutput(it, observer)
        }
        observers[observer] = listener
        this.addListener(listener)
        queue.forEach { sendOutput(it, observer) }
    }

    fun removeObserverListener(observer: StreamObserver<ReadLine>) {
        if(observers.containsKey(observer))
            this.removeListener(observers[observer]!!)
        observer.onCompleted()
    }

    private fun sendOutput(line: T?, observer: StreamObserver<ReadLine>) {
        if (line != null) {
            observer.onNext(
                fromReadLineToMsg(line.toString())
            )
        }
    }

    companion object {
        inline fun <reified X: Any> of(content: List<X> = emptyList()): OutputChannelObserver<X> {
            val queue = LinkedBlockingDeque<X>()
            queue.addAll(content)
            return OutputChannelObserver(X::class, queue)
        }
    }
}
