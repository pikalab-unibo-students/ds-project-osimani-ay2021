package it.unibo.tuprolog.solve.lpaas.server.collections

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.Listener
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.impl.AbstractOutputChannel
import it.unibo.tuprolog.solve.lpaas.solveMessage.LineEvent
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import kotlin.reflect.KClass

class OutputChannelObserver<T : Any>(
    val eventType: KClass<T>
) : OutputChannel<T>, AbstractOutputChannel<T>() {

    data class Event<T>(val item: T?, val isOver: Boolean)

    val queue: BlockingDeque<Event<T>> = LinkedBlockingDeque()

    private val observers: MutableMap<StreamObserver<LineEvent>, Listener<T?>> = mutableMapOf()

    override fun writeActually(value: T) {
        queue.add(Event(value, false))
    }

    override fun flushActually() {
        // does nothing
    }

    override fun close() {
        super.close()
        queue.add(Event(null, true))
    }

    fun addObserverListener(observer: StreamObserver<LineEvent>) {
        val listener: Listener<T?> = {
            sendOutput(it, observer)
            queue.clear()
        }
        observers[observer] = listener
        this.addListener(listener)
        queue.forEach { sendOutput(it.item, observer) }
    }

    fun removeObserverListener(observer: StreamObserver<LineEvent>) {
        if(observers.containsKey(observer))
            this.removeListener(observers[observer]!!)
    }

    private fun sendOutput(line: T?, observer: StreamObserver<LineEvent>) {
        observer.onNext(
            LineEvent.newBuilder().setLine(line.toString()).build()
        )
    }

    companion object {
        inline fun <reified X: Any> of(): OutputChannelObserver<X> {
            return OutputChannelObserver(X::class)
        }
    }
}
