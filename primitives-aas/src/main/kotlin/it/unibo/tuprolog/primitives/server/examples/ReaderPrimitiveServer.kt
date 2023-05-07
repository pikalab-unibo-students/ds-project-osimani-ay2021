package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.primitives.server.session.PrimitiveWithSession
import it.unibo.tuprolog.solve.Signature
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

val readerPrimitive = PrimitiveWithSession { request, session ->
    sequence {
        while(true) {
            val line = runBlocking {
                session.readLine(request.arguments[0].castToAtom().toString())
            }
            yield(request.replySuccess(Substitution.of(request.arguments[1].castToVar(), Atom.of(line))))
        }
    }
}

val readerPrimitiveServer = PrimitiveServerWrapper.of(
    Signature("readLine", 2), readerPrimitive)

fun main() {
    startService(readerPrimitiveServer, 8082, "customLibrary")
}