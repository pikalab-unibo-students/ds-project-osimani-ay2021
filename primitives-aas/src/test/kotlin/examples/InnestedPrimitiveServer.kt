package examples

import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper

val innestedPrimitive = DistributedPrimitiveWrapper("solve", 1) { request ->
    request.subSolve(request.arguments[0].castToStruct()).map {
            if(it.solution.isYes)
                request.replySuccess(it.solution.substitution.castToUnifier())
            else if(it.solution.isNo) {
                request.replyFail()
            } else {
                request.replyError(it.solution.exception!!)
            }
        }
}

fun main() {
    startService(innestedPrimitive, 8080, "customLibrary")
}