package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.sideffects.SideEffect

val writerPrimitive = DistributedPrimitiveWrapper("writeMessage", 2) { request ->
    sequence {
        yield(request.replyWith(true,
            SideEffect.WriteOnOutputChannels(
                Pair(
                    request.arguments[0].toString(),
                    listOf(request.arguments[1].toString())))))
    }
}

fun main() {
    startService( writerPrimitive, 8084, "customLibrary")
}