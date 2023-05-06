package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.solve.Signature

val innestedPrimitive = PrimitiveWithSession { request, session ->
    sequence {
        session.subSolve(request.arguments[0].castToStruct()).forEach {
            if(it.isYes)
                yield(request.replySuccess(it.substitution.castToUnifier()))
            else if(it.isNo) {
                yield(request.replyFail())
            } else {
                yield(request.replyException(it.exception!!))
            }
        }
    }
}

val innestedPrimitiveServer = PrimitiveServerWrapper.of(
    Signature("solve", 1), innestedPrimitive)

fun main() {
    startService(innestedPrimitiveServer, 8080, "customLibrary")
}