package examples

import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.distribuited.DistributedError
import it.unibo.tuprolog.primitives.server.distribuited.solve.DistributedPrimitiveWrapper
import it.unibo.tuprolog.solve.exception.error.TypeError
import org.gciatto.kt.math.BigInteger

val customSumPrimitive = DistributedPrimitiveWrapper("customSum", 3) { request ->
    val arg1: Term = request.arguments[0]
    val arg2: Term = request.arguments[1]
    val arg3: Term = request.arguments[2]
    if(arg1.isNumber && arg2.isNumber) {
        when(arg3) {
            is Var ->
                sequenceOf(request.replySuccess(
                    Substitution.of(arg3, Numeric.of(arg1.castToNumeric().decimalValue + arg2.castToNumeric().decimalValue)))
                )
            is Numeric -> {
                if(arg1.castToNumeric().decimalValue + arg2.castToNumeric().decimalValue == arg3.decimalValue)
                    sequenceOf(request.replySuccess())
                else sequenceOf(request.replyFail())
            }
            else ->
                sequenceOf(request.replyError(DistributedError.TypeError(
                    expectedType = TypeError.Expected.NUMBER,
                    culprit = arg3
                )))
        }
    } else {
        sequenceOf(request.replyError(DistributedError.TypeError(
            expectedType = TypeError.Expected.NUMBER,
            culprit = if(arg1.isNumber) arg2 else arg1
        )))
    }

}

fun main() {
    startService(customSumPrimitive, 8087, "customLibrary")
}