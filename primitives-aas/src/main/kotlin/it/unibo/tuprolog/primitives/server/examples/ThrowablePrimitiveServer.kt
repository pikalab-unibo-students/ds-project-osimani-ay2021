package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.exception.ResolutionException

val throwablePrimitive = DistributedPrimitiveWrapper("error",0) { request ->
    sequence {
        yield(request.replyWith(true))
        yield(request.replyError(ResolutionException(message = "I was a mistake!", context = request.context)))
    }
}

fun main() {
    startService(throwablePrimitive, 8083, "customLibrary")
}