package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.primitives.DbManager
import it.unibo.tuprolog.primitives.client.PrimitiveClientFactory.searchPrimitive
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.library.Library
import it.unibo.tuprolog.solve.library.Runtime
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper.Companion.ensuringArgumentIsStruct

val innestedPrimitive = PrimitiveWrapper.wrap<ExecutionContext>("solve", 1) { request ->
    val libraries = request.context.libraries.filter {
        it.key != Solver.prolog.defaultBuiltins.alias
    }.map {
        Library.of(it.key, DbManager.get().getLibrary(it.key).associate { signature ->
            searchPrimitive(signature.first, signature.second)
        })
    }
    val solver = Solver.prolog.solverWithDefaultBuiltins(
        unificator = request.context.unificator,
        otherLibraries = Runtime.of(libraries),
        flags = request.context.flags,
        staticKb = request.context.staticKb,
        dynamicKb = request.context.dynamicKb
    )
    //request.ensuringArgumentIsStruct(0)
    sequence {
        solver.solve(request.arguments[0].castToStruct()).forEach {
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

val innestedPrimitiveServer = PrimitiveServerWrapper.from(innestedPrimitive)

fun main() {
    startService(innestedPrimitiveServer, 8080, "customLibrary")
}