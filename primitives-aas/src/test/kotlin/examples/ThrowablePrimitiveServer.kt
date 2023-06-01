package examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.exception.HaltException

val throwablePrimitive = DistributedPrimitiveWrapper("error",0) { request ->
    sequence {
        yield(request.replyError(
            DistributedError.HaltException(
                message = "I was a mistake!",
                exitStatus = 404)))
    }
}

fun main() {
    startService(throwablePrimitive, 8083, "customLibrary")
}