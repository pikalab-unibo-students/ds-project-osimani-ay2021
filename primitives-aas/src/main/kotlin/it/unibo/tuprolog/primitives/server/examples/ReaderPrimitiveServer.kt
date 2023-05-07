package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.solve.Signature

val readerPrimitive = PrimitiveWithSession { request, session ->
    sequence {
        while(true) {
            try {
                val line = session.readLine(request.arguments[0].castToAtom().toString())
                yield(request.replySuccess(Substitution.of(request.arguments[1].castToVar(), Atom.of(line))))
            } catch (e: Exception) {
                yield(request.replyFail())
            }

        }
    }
}

val readerPrimitiveServer = PrimitiveServerWrapper.of(
    Signature("readLine", 2), readerPrimitive)

fun main() {
    startService(readerPrimitiveServer, 8082, "customLibrary")
}