package examples

import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.sideffects.SideEffect

val customWritePrimitive = DistributedPrimitiveWrapper("customWrite", 2) { request ->
    val arg1: Term = request.arguments[0]
    val arg2: Term = request.arguments[1]
    if(arg1.isAtom && arg2.isAtom) {
        sequenceOf(
            request.replySuccess(
                SideEffect.WriteOnOutputChannels(
                    Pair(arg1.toString(), listOf(arg2.toString()))
                )
            )
        )
    } else {
        sequenceOf(request.replyFail())
    }
}