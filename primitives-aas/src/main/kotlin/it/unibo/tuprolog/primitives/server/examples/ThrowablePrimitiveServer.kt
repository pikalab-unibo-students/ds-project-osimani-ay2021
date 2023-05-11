package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive
import it.unibo.tuprolog.solve.exception.ResolutionException

val throwablePrimitive = DistribuitedPrimitive { request ->
    sequence {
        yield(request.replyWith(true))
        yield(request.replyError(ResolutionException(message = "I was a mistake!", context = request.context)))
    }
}

fun main() {
    startService("error",0, throwablePrimitive, 8083, "customLibrary")
}