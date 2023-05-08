package it.unibo.tuprolog.primitives.server.examples

import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.primitives.server.PrimitiveServerFactory.startService
import it.unibo.tuprolog.primitives.server.PrimitiveServerWrapper
import it.unibo.tuprolog.solve.ExecutionContext
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.primitive.PrimitiveWrapper
import kotlinx.coroutines.yield
import org.gciatto.kt.math.BigInteger

val ntPrimitive = PrimitiveWrapper.wrap<ExecutionContext>(Signature("nt",1)) { request ->
    fun generateValues(): Sequence<Term> =
        generateSequence(BigInteger.ZERO) { it + BigInteger.ONE }.map { Integer.of(it) }

    fun checkValue(value: Integer): Boolean =
        value.intValue.signum >= 0

    when (val arg1: Term = request.arguments[0]) {
        is Var ->
            generateValues().map{ request.replySuccess(Substitution.of(arg1, it)) }
        is Integer ->
            sequenceOf(request.replyWith(checkValue(arg1)))
        else ->
            sequenceOf(request.replyFail())
    }
}

val ntPrimitiveServer = PrimitiveServerWrapper.from(ntPrimitive)

fun main() {
    startService(ntPrimitiveServer, 8081, "customLibrary")
}