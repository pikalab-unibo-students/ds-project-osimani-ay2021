package it.unibo.tuprolog.solve.lpaas.server.collections

import it.unibo.tuprolog.solve.channel.impl.AbstractInputChannel
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import kotlin.reflect.KClass

class InputChannelObserver<T : Any> (
    val eventType: KClass<T>,
    private val deque: BlockingDeque<T>
) : AbstractInputChannel<T>() {

    override fun readActually(): T? {
        return deque.takeFirst()
    }

    fun writeOnChannel(obj: T) {
        deque.putLast(obj)
    }

    companion object {
        inline fun <reified X: Any> of(content: List<X> = emptyList()): InputChannelObserver<X> {
            val deque = LinkedBlockingDeque(content)
            return InputChannelObserver(X::class, deque)
        }
    }
}
