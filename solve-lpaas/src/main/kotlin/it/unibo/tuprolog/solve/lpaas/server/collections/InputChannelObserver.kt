package it.unibo.tuprolog.solve.lpaas.server.collections

import io.grpc.stub.StreamObserver
import it.unibo.tuprolog.solve.channel.InputChannel
import it.unibo.tuprolog.solve.channel.Listener
import it.unibo.tuprolog.solve.channel.OutputChannel
import it.unibo.tuprolog.solve.channel.impl.AbstractInputChannel
import it.unibo.tuprolog.solve.channel.impl.AbstractOutputChannel
import it.unibo.tuprolog.solve.libs.oop.identifier
import it.unibo.tuprolog.solve.lpaas.solveMessage.LineEvent
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class InputChannelObserver<T : Any> private constructor(
    private val deque: BlockingDeque<T>
) : AbstractInputChannel<T>() {

    override fun readActually(): T? = deque.takeFirst()

    fun writeOnChannel(line: List<T>){
        line.forEach {
            deque.putLast(it)
        }
    }

    companion object {
        fun <X: Any> of(content: List<X> = emptyList()): InputChannelObserver<X> {
            val deque = LinkedBlockingDeque(content)
            return InputChannelObserver(deque)
        }
    }
}
