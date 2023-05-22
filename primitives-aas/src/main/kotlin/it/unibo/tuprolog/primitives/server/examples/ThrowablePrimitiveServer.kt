package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.DistributedPrimitiveWrapper

val throwablePrimitive = DistributedPrimitiveWrapper("error",0) { request ->
    sequence {
        yield(request.replyWith(true))
        yield(request.replyError(
            DistributedError.ResolutionException(
                message = "I was a mistake!")))
    }
}

fun main() {
    startService(throwablePrimitive, 8083, "customLibrary")
}