package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitive

val innestedPrimitive = DistribuitedPrimitive { request ->
    request.subSolve(request.arguments[0].castToStruct()).map {
            if(it.isYes)
                request.replySuccess(it.substitution.castToUnifier())
            else if(it.isNo) {
                request.replyFail()
            } else {
                request.replyError(it.exception!!)
            }
        }
}

fun main() {
    startService("solve", 1, innestedPrimitive, 8080, "customLibrary")
}