package examples

import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.sideffects.SideEffect

val customAssertPrimitive = DistributedPrimitiveWrapper("customAssert", 1) { request ->
    val arg1: Term = request.arguments[0]
    if(arg1.isClause) {
        sequenceOf(request.replySuccess(SideEffect.AddDynamicClauses(arg1.castToClause())))
    } else {
        sequenceOf(request.replyFail())
    }
}