package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.parsers.deserializers.deserialize
import it.unibo.tuprolog.primitives.parsers.serializers.serialize
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.solve.exception.ResolutionException
import it.unibo.tuprolog.solve.sideffects.SideEffect

val writerPrimitive = DistribuitedPrimitive { request ->
    sequence {
        yield(request.replyWith(true,
            SideEffect.WriteOnOutputChannels(
                Pair(
                    request.arguments[0].toString(),
                    listOf(request.arguments[1].toString())))))
    }
}

fun main() {
    startService("writeMessage", 2, writerPrimitive, 8084, "customLibrary")
}