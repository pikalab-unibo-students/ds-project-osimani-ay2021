package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory.searchPrimitive
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper.Companion.ensuringArgumentIsStruct

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