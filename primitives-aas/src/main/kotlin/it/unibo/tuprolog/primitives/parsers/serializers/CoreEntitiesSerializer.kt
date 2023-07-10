package it.unibo.tuprolog.primitives.parsers.serializers

import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.primitives.messages.ArgumentMsg
import it.unibo.tuprolog.primitives.messages.SignatureMsg
import it.unibo.tuprolog.primitives.messages.StructMsg
import it.unibo.tuprolog.solve.Signature
import org.gciatto.kt.math.BigDecimal

fun Term.serialize(): ArgumentMsg {
    val builder = ArgumentMsg.newBuilder()
    when(this) {
        is Var -> builder.setVar(this.name)
        is Truth -> builder.setFlag(this.isTrue)
        is Numeric -> builder.setNumeric(this.decimalValue.toDouble())
        is Atom -> builder.setAtom(this.toString())
        is Struct -> builder.setStruct(this.serialize())
    }
    return builder.build()
}

fun Struct.serialize(): StructMsg {
    return StructMsg.newBuilder()
        .setFunctor(this.functor)
        .addAllArguments(
            this.args.map { it.serialize() }
        )
        .build()
}

fun Signature.serialize(): SignatureMsg =
    SignatureMsg.newBuilder().setName(this.name).setArity(this.arity).build()

