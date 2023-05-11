package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistribuitedPrimitiveWrapper

val readerPrimitive = DistribuitedPrimitiveWrapper("readLine",2 ) { request ->
    sequence {
        while(true) {
            try {
                val line = request.readLine(request.arguments[0].castToAtom().toString())
                yield(request.replySuccess(Substitution.of(request.arguments[1].castToVar(), Atom.of(line))))
            } catch (e: Exception) {
                yield(request.replyFail())
            }
        }
    }
}

fun main() {
    startService(readerPrimitive, 8082, "customLibrary")
}