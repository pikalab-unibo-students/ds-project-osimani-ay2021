package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitiveWrapper
import it.unibo.tuprolog.solve.exception.ResolutionException

val throwablePrimitive = DistribuitedPrimitiveWrapper("error",0) { request ->
    sequence {
        yield(request.replyWith(true))
        yield(request.replyError(ResolutionException(message = "I was a mistake!", context = request.context)))
    }
}

fun main() {
    startService(throwablePrimitive, 8083, "customLibrary")
}